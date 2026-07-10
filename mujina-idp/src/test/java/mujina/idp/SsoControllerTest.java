package mujina.idp;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import mujina.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.containsString;
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
        response.then().statusCode(SC_OK);

        String html = response.getBody().asString();
        Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
        matcher.find();
        String samlResponse = new String(Base64.getDecoder().decode(matcher.group(1)));
        assertTrue(samlResponse.contains("admin@example.com"));
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
