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

import java.io.BufferedInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jcox.util.IDService;
import jcox.util.TimeService;
import jcox.saml.xml.IssuerGenerator;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.*;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class AssertionGenerator {

	private final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

	private final IssuerGenerator issuerGenerator;
	private final SubjectGenerator subjectGenerator;
	private final IDService idService;
	private final TimeService timeService;
	private final AuthnStatementGenerator authnStatementGenerator = new AuthnStatementGenerator();
	private final AttributeStatementGenerator attributeStatementGenerator = new AttributeStatementGenerator();
    private Credential signingCredential;


    public AssertionGenerator(final Credential signingCredential, String issuingEntityName, TimeService timeService, IDService idService) {
		super();
	    this.signingCredential = signingCredential;
		this.timeService = timeService;
		this.idService = idService;
		issuerGenerator = new IssuerGenerator(issuingEntityName);
		subjectGenerator = new SubjectGenerator(timeService);
	}
	
	public  Assertion generateAssertion (UsernamePasswordAuthenticationToken authToken, String recepientAssertionConsumerURL, int validForInSeconds,  String inResponseTo, DateTime authnInstant) {
		
		
		UserDetails principal =	(UserDetails) authToken.getPrincipal();
		WebAuthenticationDetails details = (WebAuthenticationDetails) authToken.getDetails();
		
		AssertionBuilder assertionBuilder = (AssertionBuilder)builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
		Assertion assertion = assertionBuilder.buildObject();
		
		Subject subject = subjectGenerator.generateSubject(recepientAssertionConsumerURL, validForInSeconds, principal.getUsername(), inResponseTo, details.getRemoteAddress());
		
		Issuer issuer = issuerGenerator.generateIssuer();
		
		AuthnStatement authnStatement = authnStatementGenerator.generateAuthnStatement(authnInstant);
		
		assertion.setIssuer(issuer);
		assertion.getAuthnStatements().add(authnStatement);
		assertion.setSubject(subject);

        // extends this
		// assertion.getAttributeStatements().add(attributeStatementGenerator.generateAttributeStatement(authToken.getAuthorities()));

        Map<String, String> attributes = new TreeMap<String,String>();
        attributes.put("urn:mace:dir:attribute-def:uid", "alle.veenstra");
        attributes.put("urn:mace:dir:attribute-def:cn", "Alle Veenstra");
        attributes.put("urn:mace:dir:attribute-def:givenName", "Alle");
        attributes.put("urn:mace:dir:attribute-def:sn", "Veenstra");
        attributes.put("urn:mace:dir:attribute-def:displayName", "Alle Veenstra");
        attributes.put("urn:mace:dir:attribute-def:mail", "a.veenstra@onehippo.com");
        attributes.put("urn:mace:terena.org:attribute-def:schacHomeOrganization", "test.surfguest.nl");
        attributes.put("urn:mace:dir:attribute-def:eduPersonPrincipalName", "alle.veenstra@SURFguest.nl");
        attributes.put("urn:oid:1.3.6.1.4.1.1076.20.100.10.10.1", "guest");

        assertion.getAttributeStatements().add(
                attributeStatementGenerator.generateAttributeStatement(attributes));

        assertion.setID(idService.generateID());
		assertion.setIssueInstant(timeService.getCurrentDateTime());

        signAssertion(assertion);

		return assertion;
	}

    private void signAssertion(final Assertion assertion) {

        Signature signature = (Signature) Configuration.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(signingCredential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_OMIT_COMMENTS);

        assertion.setSignature(signature);

        try {
            Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        } catch (MarshallingException e) {
            e.printStackTrace();
        }
        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }
}
