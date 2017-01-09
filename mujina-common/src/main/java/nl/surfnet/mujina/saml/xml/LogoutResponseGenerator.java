package nl.surfnet.mujina.saml.xml;

import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.impl.LogoutResponseBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.security.credential.Credential;

public class LogoutResponseGenerator {
  private final XMLObjectBuilderFactory builderFactory = org.opensaml.Configuration.getBuilderFactory();
  private final IDService idService;
  private final TimeService timeService;
  private final IssuerGenerator issuerGenerator;
  private final StatusGenerator statusGenerator;

  public LogoutResponseGenerator(final Credential signingCredential, String issuingEntityName, TimeService timeService,
    IDService idService) {
    this.idService = idService;
    this.timeService = timeService;
    this.issuerGenerator = new IssuerGenerator(issuingEntityName);
    this.statusGenerator = new StatusGenerator();
  }

  public LogoutResponse buildLogoutResponse() {
    LogoutResponseBuilder logoutRequestBuilder = (LogoutResponseBuilder) builderFactory.getBuilder(
      LogoutResponse.DEFAULT_ELEMENT_NAME);

    LogoutResponse logoutResponse = logoutRequestBuilder.buildObject();

    logoutResponse.setID(idService.generateID());
    logoutResponse.setIssuer(issuerGenerator.generateIssuer());
    logoutResponse.setIssueInstant(timeService.getCurrentDateTime());
    logoutResponse.setStatus(statusGenerator.generateStatus(StatusCode.SUCCESS_URI));

    return logoutResponse;
  }
}
