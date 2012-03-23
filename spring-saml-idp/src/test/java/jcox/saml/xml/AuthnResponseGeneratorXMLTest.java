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

package jcox.saml.xml;

import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;

import jcox.util.IDService;
import jcox.util.TimeService;

import org.joda.time.DateTime;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.security.credential.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class AuthnResponseGeneratorXMLTest extends AbstractXMLTest {

	private final static Logger logger = LoggerFactory
			.getLogger(AuthnResponseGeneratorXMLTest.class);
	
	final String issuerName = "some guy";
	final String userName = "dude@idp.com";
	
	final String role1 = "ROLE_1"; 
	final String role2 = "ROLE_2"; 
	
	final String inResponseTo = "12345A";
	final String responseID = "xyz";
	
	
	UsernamePasswordAuthenticationToken authToken;
	UserDetails userDeatils;
	String credentials = "secret";
	Collection<GrantedAuthorityImpl> authorities;
	
	@Mock
    TimeService timeService;
	@Mock
    IDService idService;
	@Mock WebAuthenticationDetails details;
	
	//class under test
	AuthnResponseGenerator generator;

	private String assertionConsumerURL = "https://some.service.provider/assertionConsumer";

	private int validForInSeconds = 30;

	private String clientIP = "10.40.125.1";
	SAML2ValidatorSuite validatorSuite = new SAML2ValidatorSuite();

	
	@Override
	protected void setUp() throws Exception {
		
		super.setUp();
		
		MockitoAnnotations.initMocks(this);

        Credential signingCredential = null;
		
		generator = new AuthnResponseGenerator(signingCredential, issuerName, timeService, idService);
		
		authorities = new HashSet<GrantedAuthorityImpl>();
		authorities.add(new GrantedAuthorityImpl(role1));
		authorities.add(new GrantedAuthorityImpl(role2));
		
		userDeatils = new User(userName,credentials,true, true, true, true, authorities);
		authToken = new UsernamePasswordAuthenticationToken(userDeatils,credentials,authorities);
		
		authToken.setDetails(details);
		
	}
	
	public void testGenerateFailedAuthnResponse() throws Exception{
		
		when(timeService.getCurrentDateTime()).thenReturn(new DateTime("2010-10-26T09:30:00.000Z"));
		when(idService.generateID()).thenReturn(responseID);
		when(details.getRemoteAddress()).thenReturn(clientIP );
		
		UsernameNotFoundException ex = new UsernameNotFoundException("UsernameNotFoundException!");
		
		Response response = generator.generateAuthnResponseFailure(assertionConsumerURL, inResponseTo, ex);
		
		assertNotNull(response);
		validatorSuite.validate(response);
		
		
		String xml = getAsXMLString(response);
		logger.debug( "xml is: \n{}", xml);
		
		assertXpathEvaluatesTo("0", "count(//saml2a:Assertion)", xml );
		
		
		//saml-core-2.0-os section 3.2.2 Required attributes on Response Abstract Type
		assertXpathExists("/saml2p:Response[@ID='"+responseID+"']",xml);
		assertXpathExists("/saml2p:Response[@IssueInstant='2010-10-26T09:30:00.000Z']",xml);
		assertXpathExists("/saml2p:Response[@InResponseTo='"+inResponseTo+"']",xml);
		assertXpathExists("/saml2p:Response/saml2p:Status/saml2p:StatusCode[@Value='urn:oasis:names:tc:SAML:2.0:status:Responder']",xml);
		assertXpathExists("/saml2p:Response/saml2p:Status/saml2p:StatusCode/saml2p:StatusCode[@Value='urn:oasis:names:tc:SAML:2.0:status:AuthnFailed']",xml);
		assertXpathExists("/saml2p:Response/saml2p:Status[saml2p:StatusMessage='org.springframework.security.core.userdetails.UsernameNotFoundException']",xml);
		
	}
	
	public void testGenerateAuthnResponse() throws Exception {
		
		when(timeService.getCurrentDateTime()).thenReturn(new DateTime("2010-10-26T09:30:00.000Z"));
		when(idService.generateID()).thenReturn(responseID);
		when(details.getRemoteAddress()).thenReturn(clientIP );
		
		Response response = generator.generateAuthnResponse(authToken, assertionConsumerURL, validForInSeconds, inResponseTo, new DateTime());
		
		assertNotNull(response);
		validatorSuite.validate(response);

		String xml = getAsXMLString(response);
		logger.debug( "xml is: \n{}", xml);
		
		//we will be building just 1 Assertion
		assertXpathEvaluatesTo("1", "count(//saml2a:Assertion)", xml );
		
		//saml-core-2.0-os section 3.2.2 Required attributes on Response Abstract Type
		assertXpathExists("/saml2p:Response[@ID='"+responseID+"']",xml);
		assertXpathExists("/saml2p:Response[@IssueInstant='2010-10-26T09:30:00.000Z']",xml);
		assertXpathExists("/saml2p:Response[@InResponseTo='"+inResponseTo+"']",xml);
		assertXpathExists("/saml2p:Response/saml2p:Status/saml2p:StatusCode[@Value='urn:oasis:names:tc:SAML:2.0:status:Success']",xml);
		
		//optional
		assertXpathExists("/saml2p:Response[@Destination='"+assertionConsumerURL+"']",xml);


		//Profiles for the OASIS Security Assertion Markup Language (SAML) V2.0
		//lines541-543
		//optional on the Response, but we will include one
		assertXpathExists("/saml2p:Response[saml2a:Issuer='"+issuerName+"']",xml);
		assertXpathExists( "/saml2p:Response/saml2a:Issuer[@Format='urn:oasis:names:tc:SAML:2.0:nameid-format:entity']", xml);
		
		//541 Issuer must be on the Assertion
		assertXpathExists("/saml2p:Response/saml2a:Assertion[saml2a:Issuer='"+issuerName+"']",xml);
		assertXpathExists( "/saml2p:Response/saml2a:Assertion/saml2a:Issuer[@Format='urn:oasis:names:tc:SAML:2.0:nameid-format:entity']", xml);
		
		//lines 549-553
		assertXpathExists( "/saml2p:Response/saml2a:Assertion/saml2a:Subject/saml2a:SubjectConfirmation[@Method='urn:oasis:names:tc:SAML:2.0:cm:bearer']",xml);
				      
		
		
		//lines 547
		assertXpathEvaluatesTo("1", "count(//saml2a:Assertion/saml2a:AuthnStatement)", xml );
	
		assertXpathExists( "/saml2p:Response/saml2a:Assertion/saml2a:Subject/saml2a:SubjectConfirmation[@Method='urn:oasis:names:tc:SAML:2.0:cm:bearer']",xml);

		//lines 554 to 560
		assertXpathExists( "/saml2p:Response/saml2a:Assertion/saml2a:Subject/saml2a:SubjectConfirmation/saml2a:SubjectConfirmationData[@Recipient='"+ assertionConsumerURL  +"']", xml);
		assertXpathExists( "/saml2p:Response/saml2a:Assertion/saml2a:Subject/saml2a:SubjectConfirmation/saml2a:SubjectConfirmationData[@InResponseTo='"+inResponseTo+"']", xml);
		//optional, but we will include one
		assertXpathExists( "/saml2p:Response/saml2a:Assertion/saml2a:Subject/saml2a:SubjectConfirmation/saml2a:SubjectConfirmationData[@Address='"+clientIP+"']", xml);

		
		assertXpathExists( "/saml2p:Response/saml2a:Assertion/saml2a:Subject/saml2a:SubjectConfirmation/saml2a:SubjectConfirmationData[@NotOnOrAfter='2010-10-26T09:30:30.000Z']", xml);

		
		assertXpathExists("//saml2a:Attribute[@Name='org.springframework.security.core.GrantedAuthority'][saml2a:AttributeValue='"+role1+"']",  xml);
		assertXpathExists("//saml2a:Attribute[@Name='org.springframework.security.core.GrantedAuthority'][saml2a:AttributeValue='"+role2+"']",  xml);
		assertXpathNotExists("//saml2a:Attribute[@Name='org.springframework.security.core.GrantedAuthority'][saml2a:AttributeValue='ROLE_THAT_SHOULD_NOT_BE_THERE']",  xml);

		
	}
	
	
}
