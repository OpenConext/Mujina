/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.mockoleth.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.security.*;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import nl.surfnet.mockoleth.model.SpConfiguration;
import nl.surfnet.mockoleth.saml.AuthnRequestGenerator;
import nl.surfnet.mockoleth.saml.BindingAdapter;
import nl.surfnet.mockoleth.saml.xml.EndpointGenerator;
import nl.surfnet.mockoleth.util.IDService;
import nl.surfnet.mockoleth.util.TimeService;

public class SAMLAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final static Logger logger = LoggerFactory.getLogger(SAMLAuthenticationEntryPoint.class);

    private final TimeService timeService;
    private final IDService idService;

    private String singleSignOnServiceURL;
    private String assertionConsumerServiceURL;
    private BindingAdapter bindingAdapter;
    private CredentialResolver credentialResolver;

    private SpConfiguration spConfiguration;

    public SAMLAuthenticationEntryPoint(TimeService timeService, IDService idService) {
        super();
        this.timeService = timeService;
        this.idService = idService;
    }

    @Required
    public void setSingleSignOnServiceURL(String singleSignOnServiceURL) {
        this.singleSignOnServiceURL = singleSignOnServiceURL;
    }

    @Required
    public void setAssertionConsumerServiceURL(String assertionConsumerServiceURL) {
        this.assertionConsumerServiceURL = assertionConsumerServiceURL;
    }

    @Required
    public void setBindingAdapter(BindingAdapter bindingAdapter) {
        this.bindingAdapter = bindingAdapter;
    }


    @Required
    public void setCredentialResolver(CredentialResolver credentialResolver) {
        this.credentialResolver = credentialResolver;
    }

    public void setConfiguration(final SpConfiguration spConfiguration) {
        this.spConfiguration = spConfiguration;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        AuthnRequestGenerator authnRequestGenerator = new AuthnRequestGenerator(spConfiguration.getEntityID(), timeService, idService);
        EndpointGenerator endpointGenerator = new EndpointGenerator();

        Endpoint endpoint = endpointGenerator.generateEndpoint(SingleSignOnService.DEFAULT_ELEMENT_NAME, singleSignOnServiceURL, assertionConsumerServiceURL);

        AuthnRequest authnReqeust = authnRequestGenerator.generateAuthnRequest(singleSignOnServiceURL, assertionConsumerServiceURL);

        logger.debug("Sending authnRequest to {}", singleSignOnServiceURL);

        try {
            CriteriaSet criteriaSet = new CriteriaSet();
            criteriaSet.add(new EntityIDCriteria(spConfiguration.getEntityID()));
            criteriaSet.add(new UsageCriteria(UsageType.SIGNING));

            Credential signingCredential = credentialResolver.resolveSingle(criteriaSet);
            Validate.notNull(signingCredential);

            bindingAdapter.sendSAMLMessage(authnReqeust, endpoint, signingCredential, response);
        } catch (MessageEncodingException mee) {
            logger.error("Could not send authnRequest to Identity Provider.", mee);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (org.opensaml.xml.security.SecurityException e) {
            logger.error("Unable to retrieve signing credential", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
