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

package jcox.saml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jcox.saml.xml.AuthnResponseGenerator;
import jcox.saml.xml.EndpointGenerator;
import jcox.saml.xml.SAML2ValidatorSuite;
import jcox.spring.AuthnRequestInfo;
import jcox.util.IDService;
import jcox.util.TimeService;
import jcox.saml.BindingAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;

public class SingleSignOnServiceTest {

	//args to class under test
	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;
	@Mock AuthnRequest authnRequest;
	@Mock HttpSession session;
	private String authnRequestID = "1234";
	private String assertionConsumerServiceURL = "https://sp.com/assertionConsumer";
	
	@Mock RequestDispatcher requestDispatcher;
	
	//static state
	@Mock SecurityContext context;
	@Mock UsernamePasswordAuthenticationToken authentication;

	
	//collaborators
	String issuingEntityName = "the idp"; 
	int responseValidityTimeInSeconds = 14;
	String authnResponderURI = "/protected/authnResponder.htm";
	@Mock
    TimeService timeService;
	@Mock AuthnResponseGenerator authnResponseGenerator;
	@Mock Response authResponse;
	@Mock
    EndpointGenerator endpointGenerator;
	@Mock
    BindingAdapter adapter;
	@Mock SAMLMessageContext messageContext;
	@Mock Endpoint assertionConsumerEndpoint;
	@Mock
    IDService idService;
	@Mock CredentialResolver credentialResolver;
	@Mock
    SAML2ValidatorSuite saml2ValidatorSuite;
	@Mock Credential signingCredential;

	@Captor ArgumentCaptor<AuthnRequestInfo> captor;
  
	
	//class under test
	SingleSignOnService service;
	
	
	@Before
	public void before() {
		
		MockitoAnnotations.initMocks(this);
		service = new SingleSignOnService(adapter,  authnResponderURI, saml2ValidatorSuite);
	}
	
	
	@Test
	public void testServiceThrowsMessageDecodingException() throws Exception {
		when(context.getAuthentication()).thenReturn(authentication);
		
		when(adapter.extractSAMLMessageContext(request)).thenThrow(new MessageDecodingException("MessageDecodingException!"));
		when(messageContext.getInboundSAMLMessage()).thenReturn(authnRequest);
		when(request.getRequestDispatcher(authnResponderURI)).thenReturn(requestDispatcher);
		
		service.handleRequest(request, response);
		
		verify(requestDispatcher, never()).forward(request, response);
	}
	
	@Test
	public void testServiceThrowsSecurityException() throws Exception {
		when(context.getAuthentication()).thenReturn(authentication);
		
		when(adapter.extractSAMLMessageContext(request)).thenThrow(new SecurityException("SecurityException!"));
		when(messageContext.getInboundSAMLMessage()).thenReturn(authnRequest);
		when(request.getRequestDispatcher(authnResponderURI)).thenReturn(requestDispatcher);
		
		service.handleRequest(request, response);
		
		verify(requestDispatcher, never()).forward(request, response);
	}
	
	@Test
	public void testValidateThrowsValidationException() throws Exception {
		when(context.getAuthentication()).thenReturn(authentication);
		when(adapter.extractSAMLMessageContext(request)).thenReturn(messageContext);
		when(messageContext.getInboundSAMLMessage()).thenReturn(authnRequest);
		when(request.getRequestDispatcher(authnResponderURI)).thenReturn(requestDispatcher);
		doThrow(new ValidationException("ValidationException!")).when(saml2ValidatorSuite).validate(authnRequest);
		
		service.handleRequest(request, response);
		verify(requestDispatcher, never()).forward(request, response);
	}
	
	@Test
	public void testService() throws Exception {
		when(context.getAuthentication()).thenReturn(authentication);
		when(adapter.extractSAMLMessageContext(request)).thenReturn(messageContext);
		when(messageContext.getInboundSAMLMessage()).thenReturn(authnRequest);
		when(request.getRequestDispatcher(authnResponderURI)).thenReturn(requestDispatcher);
		when(authnRequest.getID()).thenReturn(authnRequestID);
		when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(assertionConsumerServiceURL);
		when(request.getSession()).thenReturn(session);
		
		service.handleRequest(request, response);
		
		verify(requestDispatcher).forward(request, response);
		verify(session).setAttribute(eq(AuthnRequestInfo.class.getName()), captor.capture());
		assertEquals(assertionConsumerServiceURL, captor.getValue().getAssertionConumerURL());
		assertEquals(authnRequestID, captor.getValue().getAuthnRequestID());

		
	}
	

}
