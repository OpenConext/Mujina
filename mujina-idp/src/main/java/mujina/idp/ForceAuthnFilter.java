package mujina.idp;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ForceAuthnFilter extends OncePerRequestFilter {

  private SAMLMessageHandler samlMessageHandler;

  public ForceAuthnFilter(SAMLMessageHandler samlMessageHandler) {
    this.samlMessageHandler = samlMessageHandler;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String servletPath = request.getServletPath();
    if (servletPath == null || !servletPath.endsWith("SingleSignOnService") || request.getMethod().equalsIgnoreCase("GET")) {
      chain.doFilter(request, response);
      return;
    }
    SAMLMessageContext messageContext;
    try {
      messageContext = samlMessageHandler.extractSAMLMessageContext(request, response, request.getMethod().equalsIgnoreCase("POST"));
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
    AuthnRequest authnRequest = (AuthnRequest) messageContext.getInboundSAMLMessage();
    if (authnRequest.isForceAuthn()) {
      SecurityContextHolder.getContext().setAuthentication(null);
    }
    chain.doFilter(request, response);
  }
}
