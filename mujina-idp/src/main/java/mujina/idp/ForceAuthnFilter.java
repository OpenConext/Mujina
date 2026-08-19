package mujina.idp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ForceAuthnFilter extends OncePerRequestFilter {

    private final SAMLMessageHandler samlMessageHandler;

    public ForceAuthnFilter(SAMLMessageHandler samlMessageHandler) {
        this.samlMessageHandler = samlMessageHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (servletPath == null || !servletPath.endsWith("SingleSignOnService")) {
            chain.doFilter(request, response);
            return;
        }
        //This filter runs before RequestCacheAwareFilter, so on the request Spring Security replays
        //after a login redirect, the original SAMLRequest parameter isn't visible yet here (it will
        //be by the time SsoController runs). There's no pre-existing session to force-clear in that
        //case anyway, since the user just authenticated specifically to satisfy this AuthnRequest.
        if (!org.springframework.util.StringUtils.hasText(request.getParameter("SAMLRequest"))) {
            chain.doFilter(request, response);
            return;
        }
        AuthnRequest authnRequest;
        try {
            authnRequest = samlMessageHandler.parseAuthnRequest(request, request.getMethod().equalsIgnoreCase("POST"));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        if (authnRequest.isForceAuthn()) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        chain.doFilter(request, response);
    }
}
