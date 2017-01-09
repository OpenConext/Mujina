package nl.surfnet.mujina.saml;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.opensaml.saml2.core.LogoutRequest;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

public class SAMLLogoutRequestHandler implements SAMLRequestHandler<LogoutRequest> {

  private final String sloResponderURI;
  private final SessionRegistry sessionRegistry;

  public SAMLLogoutRequestHandler(String sloResponderURI, SessionRegistry sessionRegistry) {
    this.sloResponderURI = sloResponderURI;
    this.sessionRegistry = sessionRegistry;
  }

  @Override public void handleSAMLRequest(HttpServletRequest request, HttpServletResponse response, LogoutRequest logoutRequest)
    throws ServletException, IOException {
    final List<SessionInformation> sessions = sessionRegistry.getAllSessions(logoutRequest.getNameID().getValue(), false);

    for (SessionInformation sessionInformation : sessions) {
      sessionInformation.expireNow();
    }

    request.getRequestDispatcher(sloResponderURI).forward(request, response);
  }

  @Override public String getTypeLocalName() {
    return LogoutRequest.DEFAULT_ELEMENT_LOCAL_NAME;
  }
}
