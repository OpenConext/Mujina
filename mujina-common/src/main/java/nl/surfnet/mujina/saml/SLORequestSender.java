package nl.surfnet.mujina.saml;

import java.io.IOException;

import nl.surfnet.mujina.model.CommonConfiguration;
import nl.surfnet.mujina.model.Endpoint;
import nl.surfnet.mujina.saml.xml.LogoutRequestGenerator;
import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.xml.security.utils.Base64;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SLORequestSender {
  private final static Logger logger = LoggerFactory.getLogger(SLORequestSender.class);

  private final CommonConfiguration configuration;
  private final TimeService timeService;
  private final IDService idService;

  private final HttpClient httpClient;
  private final SigningService signingService;

  @Autowired public SLORequestSender(CommonConfiguration configuration, TimeService timeService, IDService idService,
    SigningService signingService) {

    this.configuration = configuration;
    this.timeService = timeService;
    this.idService = idService;
    this.signingService = signingService;

    HttpClientBuilder clientBuilder = new HttpClientBuilder();

    clientBuilder.setConnectionRetryAttempts(3);
    clientBuilder.setConnectionTimeout(2000);
    clientBuilder.setMaxConnectionsPerHost(10);
    clientBuilder.setMaxTotalConnections(20);
    this.httpClient = clientBuilder.buildClient();
    HttpParams httpParams = httpClient.getParams();
    httpParams.setParameter(HttpConnectionManagerParams.SO_TIMEOUT, 5000);

  }

  public void sendSLORequest(String subject, String reason) {
    final Endpoint sloEndpoint = configuration.getSLOEndpoint();

    if (sloEndpoint == null) {
      return;
    }

    LogoutRequestGenerator logoutRequestGenerator = new LogoutRequestGenerator(signingService, configuration.getEntityID(),
      timeService, idService);

    final LogoutRequest logoutRequest = logoutRequestGenerator.buildLogoutRequest(subject, reason);
    sendSLORequestViaPOST(logoutRequest);
  }

  private void sendSLORequestViaPOST(LogoutRequest logoutRequest) {
    final Endpoint sloEndpoint = configuration.getSLOEndpoint();

    MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
    final Marshaller marshaller = marshallerFactory.getMarshaller(logoutRequest);
    String requestString = "";
    try {
      requestString = XMLHelper.prettyPrintXML(marshaller.marshall(logoutRequest));
    }
    catch (MarshallingException e) {
      logger.error("Marshalling exception: ", e);
    }

    try {
      final PostMethod method = new PostMethod(sloEndpoint.getUrl());
      method.addParameter("SAMLRequest", Base64.encode(requestString.getBytes()));
      httpClient.executeMethod(method);
    }
    catch (IOException e) {
      logger.error("IO exception when sending SLO Request ", e);
    }
  }

}
