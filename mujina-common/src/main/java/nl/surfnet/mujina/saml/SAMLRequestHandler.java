package nl.surfnet.mujina.saml;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.opensaml.saml2.core.RequestAbstractType;

public interface SAMLRequestHandler<T extends RequestAbstractType> {
  void handleSAMLRequest(HttpServletRequest request, HttpServletResponse response, T samlRequest)
    throws ServletException, IOException;

  String getTypeLocalName();
}
