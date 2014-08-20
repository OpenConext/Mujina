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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.surfnet.mujina.model.AuthenticationMethod;
import nl.surfnet.mujina.model.IdpConfiguration;
import nl.surfnet.mujina.model.SimpleAuthentication;
import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertionGenerator {
  private static final Logger logger = LoggerFactory.getLogger(AssertionGenerator.class);
  private final XMLObjectBuilderFactory builderFactory = org.opensaml.Configuration.getBuilderFactory();

  private final IssuerGenerator issuerGenerator;
  private final SubjectGenerator subjectGenerator;
  private final IDService idService;
  private final TimeService timeService;
  private final AuthnStatementGenerator authnStatementGenerator = new AuthnStatementGenerator();
  private final AttributeStatementGenerator attributeStatementGenerator = new AttributeStatementGenerator();
  private Credential signingCredential;
  private IdpConfiguration idpConfiguration;

  public AssertionGenerator(final Credential signingCredential, String issuingEntityName, TimeService timeService, IDService idService,
      IdpConfiguration idpConfiguration) {
    super();
    this.signingCredential = signingCredential;
    this.timeService = timeService;
    this.idService = idService;
    this.idpConfiguration = idpConfiguration;
    issuerGenerator = new IssuerGenerator(issuingEntityName);
    subjectGenerator = new SubjectGenerator(timeService);
  }

  public Assertion generateAssertion(String remoteIP, SimpleAuthentication authToken, String recipientAssertionConsumerURL,
      int validForInSeconds, String inResponseTo, DateTime authnInstant, String attributeJson, String requestingEntityId) {

    AssertionBuilder assertionBuilder = (AssertionBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
    Assertion assertion = assertionBuilder.buildObject();

    String name = authToken.getName();
    Subject subject = subjectGenerator.generateSubject(recipientAssertionConsumerURL, validForInSeconds, name, inResponseTo,
        remoteIP);

    Issuer issuer = issuerGenerator.generateIssuer();

    // include audience as per spec
    final Audience audience = new AudienceBuilder().buildObject();
    audience.setAudienceURI(requestingEntityId);
    final AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
    audienceRestriction.getAudiences().add(audience);

    final Conditions conditions = new ConditionsBuilder().buildObject();
    conditions.getAudienceRestrictions().add(audienceRestriction);
    assertion.setConditions(conditions);


    AuthnStatement authnStatement = authnStatementGenerator.generateAuthnStatement(authnInstant, idpConfiguration.getEntityID());

    assertion.setIssuer(issuer);
    assertion.getAuthnStatements().add(authnStatement);
    assertion.setSubject(subject);

    final Map<String, String> attributes = new HashMap<String, String>();
    if (null != attributeJson) {
      attributes.putAll(getAttributesFromCookie(attributeJson));
    } else {
      // use the attributes from the IDP Configuration
      attributes.putAll(idpConfiguration.getAttributes());
      if (idpConfiguration.getAuthentication() == AuthenticationMethod.Method.ALL) {
        attributes.put("urn:mace:dir:attribute-def:uid", name);
        attributes.put("urn:mace:dir:attribute-def:displayName", name);
      }
    }

    assertion.getAttributeStatements().add(attributeStatementGenerator.generateAttributeStatement(attributes));

    assertion.setID(idService.generateID());
    assertion.setIssueInstant(timeService.getCurrentDateTime());

    signAssertion(assertion);

    return assertion;
  }

  private Map<String, String> getAttributesFromCookie(String attributeJson) {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> result = null;
    try {
      TypeReference<Map<String, String>> typeReference = new TypeReference<Map<String, String>>() {};
      result = mapper.readValue(attributeJson, typeReference);
    } catch (JsonParseException e) {
      logger.warn("could not parse json file for IDP attributes", e);
    } catch (JsonMappingException e) {
      logger.warn("could not parse json file for IDP attributes", e);
    } catch (IOException e) {
      logger.warn("could not parse json file for IDP attributes", e);
    }
    return result;
  }

  private void signAssertion(final Assertion assertion) {

    Signature signature = (Signature) org.opensaml.Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME)
        .buildObject(Signature.DEFAULT_ELEMENT_NAME);

    signature.setSigningCredential(signingCredential);
    signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
    signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

    assertion.setSignature(signature);

    try {
      org.opensaml.Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
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
