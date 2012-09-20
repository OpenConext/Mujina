package nl.surfnet.mujina.controllers.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nl.surfnet.mujina.model.SimpleAuthentication;
import nl.surfnet.mujina.saml.AuthnRequestGenerator;
import nl.surfnet.mujina.saml.PostBindingAdapter;
import nl.surfnet.mujina.saml.SSOSuccessAuthnResponder;
import nl.surfnet.mujina.saml.SingleSignOnService;
import nl.surfnet.mujina.saml.xml.EndpointGenerator;
import nl.surfnet.mujina.spring.security.CustomAuthenticationProvider;
import nl.surfnet.mujina.util.IDService;
import nl.surfnet.mujina.util.TimeService;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestHelper {

    @Autowired
    private SingleSignOnService singleSignOnService;

    @Autowired
    private SSOSuccessAuthnResponder ssoSuccessAuthnResponder;

    @Autowired
    private PostBindingAdapter postBindingAdapter;

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    private AuthnRequestGenerator authnRequestGenerator;

    private EndpointGenerator endpointGenerator = new EndpointGenerator();

    private String assertionConsumerServiceURL = "http://localhost:90";
    private String singleSignOnServiceURL = "http://localhost:80";
    private String issuer = "sp";

  private String protocolBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";

  public boolean responseHasAttribute(final String name, final String value, final Response resp) {
        final List<org.opensaml.saml2.core.Attribute> attributes = resp.getAssertions().get(0).getAttributeStatements().get(0).getAttributes();
        for (org.opensaml.saml2.core.Attribute attribute : attributes) {
            if (name.equals(attribute.getName())) {
                assertTrue(value.equals(attribute.getAttributeValues().get(0).getDOM().getTextContent()));
                return true;
            }
        }
        return false;
    }

    public Response doSamlLogin(final String user, final String password) throws MessageEncodingException, ServletException, IOException, XMLParserException, UnmarshallingException {
        final String authnRequest = createAuthnRequest();

        final String samlResponse = doIdpLogin(authnRequest, user, password);

        return unmarshalSamlResponse(samlResponse);
    }

    private Response unmarshalSamlResponse(final String samlResponse) throws XMLParserException, UnmarshallingException {
        BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);
        final Document doc = ppMgr.parse(new ByteArrayInputStream(samlResponse.getBytes()));
        final Element responseRoot = doc.getDocumentElement();
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(responseRoot);
        return (Response) unmarshaller.unmarshall(responseRoot);
    }

    private String doIdpLogin(final String authnRequest, final String user, final String password) throws IOException, IllegalStateException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContentType("application/x-www-form-urlencoded");
        request.setMethod("POST");
        request.setParameter("SAMLRequest", Base64.encodeBase64String(authnRequest.getBytes()));
        singleSignOnService.handleRequest(request, response);
        assertEquals(response.getStatus(), 200);

        final List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));

        SimpleAuthentication auth = new SimpleAuthentication(user, password, grantedAuthorities);
        if (customAuthenticationProvider.authenticate(auth) == null) {
            throw new IllegalStateException("Not authenticated");
        }

        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletResponse response2 = new MockHttpServletResponse();

        ssoSuccessAuthnResponder.handleRequest(request, response2);

        assertEquals(response2.getStatus(), 200);

        return readSamlRequestFromResponse(response2);
    }

  private String createAuthnRequest() throws MessageEncodingException {
    TimeService timeService = new TimeService();
    IDService idService = new IDService();
    authnRequestGenerator = new AuthnRequestGenerator(issuer, timeService, idService);
    Endpoint endpoint = endpointGenerator.generateEndpoint(org.opensaml.saml2.metadata.SingleSignOnService.DEFAULT_ELEMENT_NAME,
        singleSignOnServiceURL, assertionConsumerServiceURL);
    AuthnRequest authnReqeust = authnRequestGenerator.generateAuthnRequest(singleSignOnServiceURL, assertionConsumerServiceURL,
        protocolBinding);
    MockHttpServletResponse authnResponse = new MockHttpServletResponse();
    postBindingAdapter.sendSAMLMessage(authnReqeust, endpoint, null, null, authnResponse);
    assertEquals(authnResponse.getStatus(), 200);
    return readSamlRequestFromResponse(authnResponse);
  }

    private String readSamlRequestFromResponse(MockHttpServletResponse response) {
        final String content = new String(response.getContentAsByteArray(), Charset.forName("UTF-8"));
        Pattern pattern = Pattern.compile("value=\"(.+)\"");
        Matcher matcher = pattern.matcher(content);
        matcher.find();
        String encoded = matcher.group(1);
        return new String(Base64.decodeBase64(encoded), Charset.forName("UTF-8"));
    }
}
