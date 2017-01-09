package nl.surfnet.mujina.saml.xml;

import nl.surfnet.mujina.saml.SigningService;
import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;

public class LogoutRequestGenerator {
  private final XMLObjectBuilderFactory builderFactory = org.opensaml.Configuration.getBuilderFactory();
  private final SigningService signingService;
  private final IDService idService;
  private final TimeService timeService;
  private final IssuerGenerator issuerGenerator;

  public LogoutRequestGenerator(SigningService signingService, String issuingEntityName, TimeService timeService,
    IDService idService) {
    this.signingService = signingService;
    this.idService = idService;
    this.timeService = timeService;
    this.issuerGenerator = new IssuerGenerator(issuingEntityName);
  }

  public LogoutRequest buildLogoutRequest(String subject, String reason) {
    LogoutRequestBuilder logoutRequestBuilder = (LogoutRequestBuilder) builderFactory.getBuilder(
      LogoutRequest.DEFAULT_ELEMENT_NAME);

    LogoutRequest logoutRequest = logoutRequestBuilder.buildObject();

    logoutRequest.setID(idService.generateID());
    logoutRequest.setIssuer(issuerGenerator.generateIssuer());
    logoutRequest.setIssueInstant(timeService.getCurrentDateTime());

    NameID nameId = new NameIDBuilder().buildObject();
    nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified");
    nameId.setValue(subject);
    logoutRequest.setNameID(nameId);

    logoutRequest.setReason(reason);

    signingService.signXMLObject(logoutRequest);

    return logoutRequest;
  }
}
