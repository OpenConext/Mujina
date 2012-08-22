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

package nl.surfnet.mujina.saml.xml;


import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.signature.*;

import nl.surfnet.mujina.model.AuthenticationMethod;
import nl.surfnet.mujina.model.IdpConfiguration;
import nl.surfnet.mujina.model.SimpleAuthentication;
import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;

public class AssertionGenerator {

    private final XMLObjectBuilderFactory builderFactory = org.opensaml.Configuration.getBuilderFactory();

    private final IssuerGenerator issuerGenerator;
    private final SubjectGenerator subjectGenerator;
    private final IDService idService;
    private final TimeService timeService;
    private final AuthnStatementGenerator authnStatementGenerator = new AuthnStatementGenerator();
    private final AttributeStatementGenerator attributeStatementGenerator = new AttributeStatementGenerator();
    private Credential signingCredential;
    private IdpConfiguration idpConfiguration;

    public AssertionGenerator(final Credential signingCredential, String issuingEntityName, TimeService timeService, IDService idService, IdpConfiguration idpConfiguration) {
        super();
        this.signingCredential = signingCredential;
        this.timeService = timeService;
        this.idService = idService;
        this.idpConfiguration = idpConfiguration;
        issuerGenerator = new IssuerGenerator(issuingEntityName);
        subjectGenerator = new SubjectGenerator(timeService);
    }

    public Assertion generateAssertion(String remoteIP, SimpleAuthentication authToken, String recepientAssertionConsumerURL, int validForInSeconds, String inResponseTo, DateTime authnInstant) {
        // org.apache.xml.security.utils.ElementProxy.setDefaultPrefix(namespaceURI, prefix).



        AssertionBuilder assertionBuilder = (AssertionBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        Assertion assertion = assertionBuilder.buildObject();

        Subject subject = subjectGenerator.generateSubject(recepientAssertionConsumerURL, validForInSeconds, authToken.getName(), inResponseTo, remoteIP);

        Issuer issuer = issuerGenerator.generateIssuer();

        AuthnStatement authnStatement = authnStatementGenerator.generateAuthnStatement(authnInstant);

        assertion.setIssuer(issuer);
        assertion.getAuthnStatements().add(authnStatement);
        assertion.setSubject(subject);

        // extends this
        // assertion.getAttributeStatements().add(attributeStatementGenerator.generateAttributeStatement(authToken.getAuthorities()));

        final Map<String,String> attributes = new HashMap<String, String>();
        attributes.putAll(idpConfiguration.getAttributes());

        if (idpConfiguration.getAuthentication() == AuthenticationMethod.Method.ALL) {
            attributes.put("urn:mace:dir:attribute-def:uid", authToken.getName());
        }

        assertion.getAttributeStatements().add(attributeStatementGenerator.generateAttributeStatement(attributes));

        assertion.setID(idService.generateID());
        assertion.setIssueInstant(timeService.getCurrentDateTime());

        signAssertion(assertion);

        return assertion;
    }

    private void signAssertion(final Assertion assertion) {

        Signature signature = (Signature) org.opensaml.Configuration.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(signingCredential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        KeyInfoGenerator kiGenerator = SecurityHelper.getKeyInfoGenerator(signingCredential, null, null);
        KeyInfo keyInfo;
        try {
            keyInfo = kiGenerator.generate(signingCredential);
        } catch (org.opensaml.xml.security.SecurityException e) {
            throw new RuntimeException("Cannot generate KeyInfo for signing the assertion", e);
        }
        signature.setKeyInfo(keyInfo);

        assertion.setSignature(signature);

        try {
            org.opensaml.Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
            Signer.signObject(signature);
        } catch (MarshallingException e) {
            throw new RuntimeException("Cannot Marshall the Signature for the Assertion", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Cannot Sign Signature", e);
        }
    }


}
