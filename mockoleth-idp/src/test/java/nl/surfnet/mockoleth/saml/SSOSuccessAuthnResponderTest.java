/*
*   Copyright 2012 SURFnet.nl
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

package nl.surfnet.mockoleth.saml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.surfnet.mockoleth.saml.xml.AuthnResponseGenerator;
import nl.surfnet.mockoleth.saml.xml.EndpointGenerator;
import nl.surfnet.mockoleth.spring.AuthnRequestInfo;
import nl.surfnet.mockoleth.util.IDService;
import nl.surfnet.mockoleth.util.TimeService;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SSOSuccessAuthnResponderTest {

	//args to class under test
	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;
	@Mock AuthnRequestInfo info;
	

	private String authnRequestID = "43";
	private String assertionConumerServiceURL = "https://serviceprovider.com/assertionconsumer";

	private final DateTime authnInstant = new DateTime();
	@Mock HttpSession session;

	//static state
	@Mock SecurityContext context;
	@Mock UsernamePasswordAuthenticationToken authentication;

	
	//collaborators
	String issuingEntityName = "the idp"; 
	int responseValidityTimeInSeconds = 14;
	@Mock
    TimeService timeService;
	@Mock
    AuthnResponseGenerator authnResponseGenerator;
	@Mock Response authResponse;
	@Mock
    EndpointGenerator endpointGenerator;
	@Mock
    BindingAdapter adapter;
	@Mock Endpoint assertionConsumerEndpoint;
	@Mock
    IDService idService;
	@Mock CredentialResolver credentialResolver;
	
	@Mock Credential signingCredential;
	
	
	//class under test
	SSOSuccessAuthnResponder responder;
	
	@Before
	public void before() {
		
		MockitoAnnotations.initMocks(this);
		
		responder = new SSOSuccessAuthnResponder(issuingEntityName, timeService, idService, adapter, credentialResolver);
		
		responder.setResponseValidityTimeInSeconds(responseValidityTimeInSeconds);
		responder.authnResponseGenerator = authnResponseGenerator;
		responder.endpointGenerator = endpointGenerator;
		responder.signingCredential = signingCredential;
		 SecurityContextHolder.setContext(context);
		
	}
	
	
	@Test
	public void testServletIsAccessedDirectly() throws Exception {
		
		when(request.getSession()).thenReturn(session);
		when(session.getAttribute(AuthnRequestInfo.class.getName())).thenReturn(null);

		
		responder.handleRequest(request, response);

		verify(adapter,never()).sendSAMLMessage((SignableSAMLObject) any(), (Endpoint) any(), eq(signingCredential), eq(response));
		verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
	}
	
	@Test
	public void encodeThrowsMessageEncodingException() throws Exception {
		
		when(context.getAuthentication()).thenReturn(authentication);
		
		when(request.getSession()).thenReturn(session);
		when(session.getAttribute(AuthnRequestInfo.class.getName())).thenReturn(info);

		when(session.getCreationTime()).thenReturn(authnInstant.getMillis());
		when(info.getAssertionConumerURL()).thenReturn(assertionConumerServiceURL);
		when(info.getAuthnRequestID()).thenReturn(authnRequestID);
		when(endpointGenerator.generateEndpoint(org.opensaml.saml2.metadata.AssertionConsumerService.DEFAULT_ELEMENT_NAME, assertionConumerServiceURL, null)).thenReturn(assertionConsumerEndpoint);
		when(authnResponseGenerator.generateAuthnResponse(authentication, assertionConumerServiceURL, responseValidityTimeInSeconds, authnRequestID,authnInstant)).thenReturn(authResponse);		
		doThrow(new MessageEncodingException("MessageEncodingException!")).when(adapter).sendSAMLMessage(authResponse, assertionConsumerEndpoint, signingCredential, response);
		
		responder.handleRequest(request, response);
			
		verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
	}
	
	@Test
	public void testService() throws Exception {

		when(context.getAuthentication()).thenReturn(authentication);
		when(request.getSession()).thenReturn(session);
		when(session.getAttribute(AuthnRequestInfo.class.getName())).thenReturn(info);
		when(session.getCreationTime()).thenReturn(authnInstant.getMillis());
		when(info.getAssertionConumerURL()).thenReturn(assertionConumerServiceURL);
		when(info.getAuthnRequestID()).thenReturn(authnRequestID);
		when(endpointGenerator.generateEndpoint(org.opensaml.saml2.metadata.AssertionConsumerService.DEFAULT_ELEMENT_NAME, assertionConumerServiceURL, null)).thenReturn(assertionConsumerEndpoint);
		when(authnResponseGenerator.generateAuthnResponse(authentication, assertionConumerServiceURL, responseValidityTimeInSeconds, authnRequestID,authnInstant)).thenReturn(authResponse);		
		
		responder.handleRequest(request, response);
		
		verify(adapter).sendSAMLMessage(authResponse, assertionConsumerEndpoint, signingCredential, response);
		verify(session).removeAttribute(AuthnRequestInfo.class.getName());
	}
	
	@Test
	public void testAfterPropertiesSet() throws Exception {
		when(credentialResolver.resolveSingle((CriteriaSet) any())).thenReturn(signingCredential);
		responder.afterPropertiesSet();
		assertNotNull(responder.authnResponseGenerator);
		assertEquals(responder.signingCredential, signingCredential);
		
	}
	
}
