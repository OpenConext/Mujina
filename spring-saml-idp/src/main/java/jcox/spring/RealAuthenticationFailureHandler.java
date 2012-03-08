/*
*   Copyright 2010 James Cox <james.s.cox@gmail.com>
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package jcox.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcox.saml.BindingAdapter;
import jcox.saml.xml.AuthnResponseGenerator;
import jcox.saml.xml.EndpointGenerator;
import jcox.util.IDService;
import jcox.util.TimeService;

import org.apache.commons.lang.Validate;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * If there is a AuthnRequestInfo in session this failure handler will respond to the Service
 * Provider with a SAML Response detailing the authn failure.  The SP can then generate a new
 * autnRequest that will send the user back to the login form - details of the original failure 
 * should be visible on the login form.
 * 
 * 
 * If there is no AuthnRequestInfo in session (this is a direct form based login attempt to the
 * IDP) delegate the failure to an injected SimpleURLFailureHandler. 
 * 
 *
 * 
 * @author jcox
 *
 */
public class RealAuthenticationFailureHandler implements AuthenticationFailureHandler, InitializingBean {


	private final static Logger logger = LoggerFactory
			.getLogger(RealAuthenticationFailureHandler.class);
	
	private final TimeService timeService;
	private final IDService idService;
	private final String issuingEntityName;
	private final CredentialResolver credentialResolver;
	private final BindingAdapter bindingAdapter;
	private final AuthenticationFailureHandler nonSSOAuthnFailureHandler;
	
	Credential signingCredential;
	EndpointGenerator endpointGenerator;
	AuthnResponseGenerator authnResponseGenerator;
	


	public RealAuthenticationFailureHandler(TimeService timeService,
			IDService idService, String issuingEntityName,
			CredentialResolver credentialResolver, BindingAdapter bindingAdapter,
			AuthenticationFailureHandler nonSSOAuthnFailureHandler) {
		super();
		this.timeService = timeService;
		this.idService = idService;
		this.issuingEntityName = issuingEntityName;
		this.credentialResolver = credentialResolver;
		this.bindingAdapter = bindingAdapter;
		this.nonSSOAuthnFailureHandler = nonSSOAuthnFailureHandler;
	}

	
	@Override
	public void afterPropertiesSet() throws Exception {
		authnResponseGenerator = new AuthnResponseGenerator(issuingEntityName, timeService, idService);
		endpointGenerator = new EndpointGenerator();
		
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIDCriteria(issuingEntityName));
		criteriaSet.add(new UsageCriteria(UsageType.SIGNING));

		signingCredential = credentialResolver.resolveSingle(criteriaSet);
		Validate.notNull(signingCredential);
		
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authenticationException)
			throws IOException, ServletException {
		logger.debug("commencing RealAuthenticationFailureHandler because of {}", authenticationException.getClass());

		AuthnRequestInfo authnRequestInfo = (AuthnRequestInfo) request.getSession().getAttribute(AuthnRequestInfo.class.getName());
		
		if(authnRequestInfo == null) {
			logger.warn("Could not find AuthnRequestInfo on the request.  Delegating to nonSSOAuthnFailureHandler.");
			nonSSOAuthnFailureHandler.onAuthenticationFailure(request, response, authenticationException);
			return;
		}
		
		logger.debug("AuthnRequestInfo is {}", authnRequestInfo );
		
		request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, authenticationException);
		
		Response authResponse = authnResponseGenerator.generateAuthnResponseFailure(authnRequestInfo.getAssertionConumerURL(), authnRequestInfo.getAuthnRequestID(), authenticationException);
		Endpoint endpoint = endpointGenerator.generateEndpoint(AssertionConsumerService.DEFAULT_ELEMENT_NAME, authnRequestInfo.getAssertionConumerURL(), null);
		
		request.getSession().removeAttribute(AuthnRequestInfo.class.getName());
		
		try {
			bindingAdapter.sendSAMLMessage(authResponse, endpoint, signingCredential, response);
		} catch (MessageEncodingException mee) {
			logger.error("Exception encoding SAML message", mee);
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
		
		
	}

}
