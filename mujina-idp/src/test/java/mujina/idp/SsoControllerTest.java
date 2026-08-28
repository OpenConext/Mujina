package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import mujina.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TestPropertySource(properties = {"idp.expires:" + (Integer.MAX_VALUE / 2 - 1), "idp.clock_skew: " + (Integer.MAX_VALUE / 2 - 1)})
public class SsoControllerTest extends AbstractIntegrationTest {

    @Test
    public void singleSignOnServiceGet() throws Exception {
        assertSuccessfulSSO(doSingleSignOn(false, false));
    }

    @Test
    public void singleSignOnServicePost() throws Exception {
        assertSuccessfulSSO(doSingleSignOn(true, false));
    }

    @Test
    public void singleSignOnServiceGetForceAuthn() throws Exception {
        assertForcedReauthentication(doSingleSignOn(false, true));
    }

    @Test
    public void singleSignOnServicePostForceAuthn() throws Exception {
        assertForcedReauthentication(doSingleSignOn(true, true));
    }

    // Regression test: a ForceAuthn AuthnRequest sent via HTTP-Redirect binding stays embedded in the
    // URL that Spring Security replays after the forced re-login. Without ForceAuthnFilter remembering
    // it already forced re-authentication for this exact AuthnRequest ID, that replay was treated as a
    // brand new ForceAuthn request, clearing the freshly established session again and bouncing back to
    // /login forever.
    @Test
    public void singleSignOnServiceGetForceAuthnCompletesOnReplayAfterReLogin() throws Exception {
        CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);
        List<String[]> requestParams = redirectBindingParams(true);

        // First visit: the pre-existing session is forcibly cleared and the user is sent to /login,
        // exactly like singleSignOnServiceGetForceAuthn above.
        assertForcedReauthentication(singleSignOnServiceRequest(requestParams, cookieFilter));

        // The user re-authenticates within the same browser session (same cookieFilter).
        given()
                .formParam("username", "admin")
                .formParam("password", "secret")
                .filter(cookieFilter)
                .post("/login")
                .then()
                .statusCode(SC_MOVED_TEMPORARILY);

        // Spring Security replays the exact same SingleSignOnService request (same AuthnRequest ID,
        // still present in the URL) - this must now complete SSO, not force yet another re-login.
        assertSuccessfulSSO(singleSignOnServiceRequest(requestParams, cookieFilter));
    }

    // Regression test: SAMLAttributeAuthenticationFilter#setDetails stashes every /login form field
    // (except username/password) verbatim into the Authentication's Details, including the
    // authn-context-class-ref-value hidden field. SsoController#attributes must strip that entry back
    // out again so it isn't sent to the SP as a bogus SAML attribute, while the value itself still
    // drives the AuthnContextClassRef of the response.
    @Test
    public void singleSignOnServiceRemovesAuthnContextClassRefValueFromAttributes() throws Exception {
        String customAuthnContextClassRef = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
        CookieFilter cookieFilter = loginWithExtraParams("admin", "secret",
                Map.of("authn-context-class-ref-value", customAuthnContextClassRef));

        String samlResponse = decodeSAMLResponse(singleSignOnServiceRequest(redirectBindingParams(false), cookieFilter));

        assertTrue(samlResponse.contains(customAuthnContextClassRef));
        assertFalse(samlResponse.contains("authn-context-class-ref-value"));
    }

    // Regression test: when the 'Persist me' checkbox is submitted on /login, SsoController#attributes
    // must replace the existing configured user with a new FederatedUserAuthenticationToken carrying the
    // attributes entered on the login form, so a subsequent login reuses them without re-entering them.
    @Test
    public void singleSignOnServicePersistsUserWhenPersistMeChecked() throws Exception {
        CookieFilter cookieFilter = loginWithExtraParams("user", "secret", Map.of(
                "persist-me", "on",
                "urn:mace:dir:attribute-def:new", "value1"));

        singleSignOnServiceRequest(redirectBindingParams(false), cookieFilter).then().statusCode(SC_OK);

        List<FederatedUserAuthenticationToken> users = idpConfiguration.getUsers();
        assertEquals(2, users.size());
        FederatedUserAuthenticationToken persisted = users.stream()
                .filter(user -> "user".equals(user.getPrincipal()))
                .findFirst()
                .orElseThrow();
        assertTrue(persisted.getAttributes().get("urn:mace:dir:attribute-def:new").contains("value1"));
    }

    private CookieFilter loginWithExtraParams(String username, String password, Map<String, String> extraParams) {
        CookieFilter cookieFilter = new CookieFilter();

        RequestSpecification requestSpecification = given()
                .formParam("username", username)
                .formParam("password", password);
        extraParams.forEach(requestSpecification::formParam);
        requestSpecification
                .filter(cookieFilter)
                .post("/login")
                .then()
                .statusCode(SC_MOVED_TEMPORARILY);

        return cookieFilter;
    }

    private String decodeSAMLResponse(Response response) {
        response.then().statusCode(SC_OK);
        String html = response.getBody().asString();
        Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
        matcher.find();
        return new String(Base64.getDecoder().decode(matcher.group(1)));
    }

    private Response singleSignOnServiceRequest(List<String[]> requestParams, CookieFilter cookieFilter) {
        RequestSpecification requestSpecification = given();
        requestParams.forEach(param -> requestSpecification.param(param[0], param[1]));
        // GET requests would otherwise auto-follow a 302 to /login, masking the forced re-authentication.
        requestSpecification.filter(cookieFilter).redirects().follow(false);
        return requestSpecification.get("/SingleSignOnService");
    }

    private Response doSingleSignOn(boolean post, boolean forceAuthn) throws Exception {
        CookieFilter cookieFilter = login("admin", "secret", SC_MOVED_TEMPORARILY);

        List<String[]> requestParams = post ? postBindingParams(forceAuthn) : redirectBindingParams(forceAuthn);

        RequestSpecification requestSpecification = given();
        if (post) {
            requestParams.forEach(param -> requestSpecification.formParam(param[0], param[1]));
        } else {
            requestParams.forEach(param -> requestSpecification.param(param[0], param[1]));
        }
        // GET requests would otherwise auto-follow a 302 to /login, masking the forced re-authentication.
        requestSpecification.filter(cookieFilter).redirects().follow(false);

        String path = "/SingleSignOnService";
        return post ? requestSpecification.post(path) : requestSpecification.get(path);
    }

    private void assertSuccessfulSSO(Response response) {
        assertTrue(decodeSAMLResponse(response).contains("admin@example.com"));
    }

    private void assertForcedReauthentication(Response response) {
        // The prior login session must have been invalidated by ForceAuthn, so the SSO
        // request is redirected to the login page instead of completing SSO right away.
        response
                .then()
                .statusCode(SC_MOVED_TEMPORARILY)
                .header("Location", containsString("/login"));
    }

}
