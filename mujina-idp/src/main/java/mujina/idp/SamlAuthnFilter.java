package mujina.idp;

import mujina.saml.ParameterDecodingHttpServletRequestWrapper;
import mujina.saml.SAMLAuthentication;
import mujina.saml.SAMLPrincipal;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class SamlAuthnFilter extends OncePerRequestFilter implements AuthenticationEntryPoint {

  private final SAMLMessageHandler samlMessageHandler;

  public SamlAuthnFilter(SAMLMessageHandler samlMessageHandler) {
    this.samlMessageHandler = samlMessageHandler;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    if (authenticationNotRequired()) {
      sendAuthResponse(response);
      return;
    }

    if (!isSAML(request)) {
      if (!request.getRequestURI().contains("test")) {
        throw new IllegalArgumentException("No SAMLRequest or SAMLResponse query path parameter, invalid SAML 2 HTTP Redirect message");
      }
      //sendAuthnRequest to EB
      //SecurityContextHolder.getContext().setAuthentication(new SAMLAuthentication(new NoProxySAMLPrincipal()));
      request.getRequestDispatcher("/saml/login").forward(request, response);
      return;
    }

    //The SAMLRequest parameters are urlEncoded and the extraction expects unencoded parameters
    SAMLMessageContext messageContext = null;
    try {
      messageContext = samlMessageHandler.extractSAMLMessageContext(new ParameterDecodingHttpServletRequestWrapper(request));
    } catch (ValidationException | SecurityException e) {
      throw new RuntimeException(e);
    }

    AuthnRequest authnRequest = (AuthnRequest) messageContext.getInboundSAMLMessage();

    SAMLPrincipal principal = new SAMLPrincipal(authnRequest.getIssuer().getValue(), authnRequest.getID(),
      authnRequest.getAssertionConsumerServiceURL(), messageContext.getRelayState());

    SecurityContextHolder.getContext().setAuthentication(new SAMLAuthentication(principal));

    //forward to login page will trigger the sending of AuthRequest to the IdP
    request.getRequestDispatcher("/login").forward(request, response);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {
    if (!SAMLUtil.processFilter("/saml/idp", request)) {
      chain.doFilter(request, response);
      return;
    }
    commence(request, response, null);
  }

  private void sendAuthResponse(HttpServletResponse response) {
    SAMLPrincipal principal = (SAMLPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    try {
      samlMessageHandler.sendAuthnResponse(principal, response);
    } catch (MarshallingException | SignatureException | MessageEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean authenticationNotRequired() {
    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    return existingAuth != null && existingAuth.getPrincipal() instanceof SAMLPrincipal && existingAuth.isAuthenticated();
  }

  private boolean isSAML(HttpServletRequest request) {
    return StringUtils.hasText(request.getParameter("SAMLResponse"))
      || StringUtils.hasText(request.getParameter("SAMLRequest"));

  }

}
