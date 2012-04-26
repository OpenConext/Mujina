package nl.surfnet.mujina.controllers.tests;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import nl.surfnet.mujina.model.SpConfiguration;
import nl.surfnet.mujina.spring.SAMLAuthenticationEntryPoint;
import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:applicationContext-sp-config.xml",
        "classpath:applicationContext-property-mappings.xml",
        "classpath:applicationContext-spring-security.xml",
        "classpath:api-servlet.xml"})
public class SpRestAPITest {

    public static final String SIGN_ON_SERVICE_URL = "http://www.test.nl/";

    @Autowired
    private SpConfiguration configuration;

    @Autowired
    private SAMLAuthenticationEntryPoint samlAuthenticationEntryPoint;

    @Test
    public void testSSOUrlRedirect() throws IOException, ServletException {
        configuration.setSingleSignOnServiceURL(SIGN_ON_SERVICE_URL);


        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setQueryString("/user.jsp");
        samlAuthenticationEntryPoint.commence(request, response, null);

        final String content = new String(response.getContentAsByteArray());
        assertTrue(content.contains("www.test.nl"));

        configuration.reset();
    }

}
