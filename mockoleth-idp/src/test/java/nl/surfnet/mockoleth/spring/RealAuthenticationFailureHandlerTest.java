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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import nl.surfnet.mockoleth.saml.BindingAdapter;
import nl.surfnet.mockoleth.saml.xml.AuthnResponseGenerator;
import nl.surfnet.mockoleth.saml.xml.EndpointGenerator;
import nl.surfnet.mockoleth.util.IDService;
import nl.surfnet.mockoleth.util.TimeService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RealAuthenticationFailureHandlerTest {

    //args to class under test
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    AuthenticationException authenticationException;
    @Mock
    AuthnRequestInfo info;
    @Mock
    HttpSession session;
    private String authnRequestID = "43";
    private String assertionConumerServiceURL = "https://serviceprovider.com/assertionconsumer";

    //collabs
    @Mock
    TimeService timeService;
    @Mock
    IDService idService;
    final String issuer = "the idp";

    @Mock
    BindingAdapter bindingAdapter;
    @Mock
    CredentialResolver credentialResolver;
    @Mock
    Credential signingCredential;

    @Mock
    EndpointGenerator endpointGenerator;
    @Mock
    AuthnResponseGenerator authnResponseGenerator;
    @Mock
    Response authResponse;
    @Mock
    Endpoint assertionConsumerEndpoint;
    @Mock
    AuthenticationFailureHandler nonSSOAuthnFailureHandler;

    //class under test
    RealAuthenticationFailureHandler handler;


    @Before
    public void before() {

        MockitoAnnotations.initMocks(this);

        handler = new RealAuthenticationFailureHandler(timeService, idService, issuer, credentialResolver, bindingAdapter, nonSSOAuthnFailureHandler);

        handler.authnResponseGenerator = authnResponseGenerator;
        handler.endpointGenerator = endpointGenerator;
        handler.signingCredential = signingCredential;
    }


    @Test
    public void testNoAuthnRequestInSession() throws Exception {

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(AuthnRequestInfo.class.getName())).thenReturn(null);


        handler.onAuthenticationFailure(request, response, authenticationException);

        verify(bindingAdapter, never()).sendSAMLMessage((SignableSAMLObject) any(), (Endpoint) any(), eq(signingCredential), eq(response));
        verify(nonSSOAuthnFailureHandler).onAuthenticationFailure(request, response, authenticationException);
    }

    @Test
    public void testOnAuthenticationFailure() throws Exception {

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(AuthnRequestInfo.class.getName())).thenReturn(info);
        when(info.getAssertionConumerURL()).thenReturn(assertionConumerServiceURL);
        when(info.getAuthnRequestID()).thenReturn(authnRequestID);
        when(endpointGenerator.generateEndpoint(org.opensaml.saml2.metadata.AssertionConsumerService.DEFAULT_ELEMENT_NAME, assertionConumerServiceURL, null)).thenReturn(assertionConsumerEndpoint);
        when(authnResponseGenerator.generateAuthnResponseFailure(assertionConumerServiceURL, authnRequestID, authenticationException)).thenReturn(authResponse);
        ;

        handler.onAuthenticationFailure(request, response, authenticationException);

        verify(bindingAdapter).sendSAMLMessage(authResponse, assertionConsumerEndpoint, signingCredential, response);
        verify(session).removeAttribute(AuthnRequestInfo.class.getName());
        verify(session).setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, authenticationException);
    }

    @Test
    public void encodeThrowsMessageEncodingException() throws Exception {

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(AuthnRequestInfo.class.getName())).thenReturn(info);
        when(info.getAssertionConumerURL()).thenReturn(assertionConumerServiceURL);
        when(info.getAuthnRequestID()).thenReturn(authnRequestID);
        when(endpointGenerator.generateEndpoint(org.opensaml.saml2.metadata.AssertionConsumerService.DEFAULT_ELEMENT_NAME, assertionConumerServiceURL, null)).thenReturn(assertionConsumerEndpoint);
        when(authnResponseGenerator.generateAuthnResponseFailure(assertionConumerServiceURL, authnRequestID, authenticationException)).thenReturn(authResponse);
        ;
        doThrow(new MessageEncodingException("MessageEncodingException!")).when(bindingAdapter).sendSAMLMessage(authResponse, assertionConsumerEndpoint, signingCredential, response);

        handler.onAuthenticationFailure(request, response, authenticationException);

        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        when(credentialResolver.resolveSingle((CriteriaSet) any())).thenReturn(signingCredential);

        handler.afterPropertiesSet();

        assertNotNull(handler.authnResponseGenerator);
        assertEquals(handler.signingCredential, signingCredential);

    }

}
