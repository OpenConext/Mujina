package mujina.idp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ForceAuthnFilter extends OncePerRequestFilter {

    //Session attribute holding the AuthnRequest IDs that have already had a forced
    //re-authentication performed for them - see the isForceAuthn() branch below.
    private static final String FORCE_AUTHN_HANDLED_IDS = ForceAuthnFilter.class.getName() + ".HANDLED_IDS";

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
        if (authnRequest.isForceAuthn() && isFirstTimeSeen(request, authnRequest.getID())) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        chain.doFilter(request, response);
    }

    //Returns true only the first time a given AuthnRequest ID is seen. With HTTP-Redirect binding,
    //the whole AuthnRequest (including its ID) is embedded in the URL, so it is still visible on the
    //request Spring Security replays after a successful login. Without this check, that replay would
    //be treated as a brand new ForceAuthn request and the freshly established authentication would be
    //cleared again, redirecting back to /login forever.
    @SuppressWarnings("unchecked")
    private boolean isFirstTimeSeen(HttpServletRequest request, String authnRequestId) {
        HttpSession session = request.getSession();
        Set<String> handledIds = (Set<String>) session.getAttribute(FORCE_AUTHN_HANDLED_IDS);
        if (handledIds == null) {
            handledIds = new HashSet<>();
            session.setAttribute(FORCE_AUTHN_HANDLED_IDS, handledIds);
        }
        return handledIds.add(authnRequestId);
    }
}
