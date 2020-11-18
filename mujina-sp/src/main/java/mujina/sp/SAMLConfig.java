package mujina.sp;

import mujina.api.SpConfiguration;
import mujina.saml.UpgradedSAMLBootstrap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.processor.*;
import org.springframework.security.saml.websso.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Typically this should all be done by default convention in the Spring SAML library,
 * but as Spring SAML is not build with conventions over configuration we do all the
 * plumbing ourselves.
 */
@Configuration
public class SAMLConfig {

  @Autowired
  private Environment environment;

  @Bean
  public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
    return new MultiThreadedHttpConnectionManager();
  }

  @Bean
  public HttpClient httpClient() {
    return new HttpClient(multiThreadedHttpConnectionManager());
  }

  private HTTPArtifactBinding artifactBinding(ParserPool parserPool,
                                              VelocityEngine velocityEngine,
                                              ArtifactResolutionProfile artifactResolutionProfile) {
    return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile);
  }

  @Bean
  @Autowired
  public HTTPSOAP11Binding soapBinding(ParserPool parserPool) {
    return new HTTPSOAP11Binding(parserPool);
  }

  @Bean
  @Autowired
  public HTTPPostBinding httpPostBinding(ParserPool parserPool, VelocityEngine velocityEngine, @Value("${sp.compare_endpoints}") boolean compareEndpoints) {
    HTTPPostEncoder encoder = new HTTPPostEncoder(velocityEngine, "/templates/saml2-post-binding.vm");
    HTTPPostDecoder decoder = new HTTPPostDecoder(parserPool);
    if (!compareEndpoints) {
      decoder.setURIComparator((uri1, uri2) -> true);
    }
    return new HTTPPostBinding(parserPool, decoder, encoder);
  }

  @Bean
  @Autowired
  public HTTPRedirectDeflateBinding httpRedirectDeflateBinding(ParserPool parserPool) {
    return new HTTPRedirectDeflateBinding(parserPool);
  }

  @Bean
  @Autowired
  public HTTPSOAP11Binding httpSOAP11Binding(ParserPool parserPool) {
    return new HTTPSOAP11Binding(parserPool);
  }

  @Bean
  @Autowired
  public HTTPPAOS11Binding httpPAOS11Binding(ParserPool parserPool) {
    return new HTTPPAOS11Binding(parserPool);
  }

  @Autowired
  @Bean
  public SAMLProcessor processor(VelocityEngine velocityEngine,
                                 ParserPool parserPool,
                                 SpConfiguration spConfiguration,
                                 @Value("${sp.compare_endpoints}") boolean compareEndpoints) {
    ArtifactResolutionProfile artifactResolutionProfile = new ArtifactResolutionProfileImpl(httpClient());
    Collection<SAMLBinding> bindings = new ArrayList<>();
    bindings.add(httpRedirectDeflateBinding(parserPool));
    bindings.add(httpPostBinding(parserPool, velocityEngine, compareEndpoints));
    bindings.add(artifactBinding(parserPool, velocityEngine, artifactResolutionProfile));
    bindings.add(httpSOAP11Binding(parserPool));
    bindings.add(httpPAOS11Binding(parserPool));
    // return new ConfigurableSAMLProcessor(bindings, spConfiguration);

    // Support multiple idp
    return new SAMLProcessorImpl(bindings);

  }

  @Bean
  public static SAMLBootstrap sAMLBootstrap() {
    return new UpgradedSAMLBootstrap();
  }

  @Bean
  public SAMLDefaultLogger samlLogger() {
    return new SAMLDefaultLogger();
  }

  @Bean
  public WebSSOProfileConsumer webSSOprofileConsumer() {
    WebSSOProfileConsumerImpl webSSOProfileConsumer = environment.acceptsProfiles(Profiles.of("test")) ?
      new WebSSOProfileConsumerImpl() {
        @Override
        @SuppressWarnings("unchecked")
        protected void verifyAssertion(Assertion assertion, AuthnRequest request, SAMLMessageContext context) throws AuthenticationException, SAMLException, org.opensaml.xml.security.SecurityException, ValidationException, DecryptionException {
          //nope
          context.setSubjectNameIdentifier(assertion.getSubject().getNameID());
        }
      } : new WebSSOProfileConsumerImpl();
    webSSOProfileConsumer.setResponseSkew(15 * 60);
    return webSSOProfileConsumer;
  }

  @Bean
  public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
    return new WebSSOProfileConsumerHoKImpl();
  }

  @Bean
  @Autowired
  public WebSSOProfile webSSOprofile(SAMLProcessor samlProcessor) {
    WebSSOProfileImpl webSSOProfile = new WebSSOProfileImpl();
    webSSOProfile.setProcessor(samlProcessor);
    return webSSOProfile;
  }

  @Bean
  public WebSSOProfileECPImpl ecpprofile() {
    return new WebSSOProfileECPImpl();
  }

}
