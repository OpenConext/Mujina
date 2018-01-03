package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.saml.KeyStoreLocator;
import mujina.saml.ProxiedSAMLContextProviderLB;
import mujina.saml.UpgradedSAMLBootstrap;
import org.opensaml.common.binding.decoding.URIComparator;
import org.opensaml.common.binding.security.IssueInstantRule;
import org.opensaml.common.binding.security.MessageReplayRule;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostSimpleSignEncoder;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.util.storage.ReplayCache;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.StaticSecurityPolicyResolver;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer extends WebMvcConfigurerAdapter {

  @Autowired
  private Environment environment;

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/login").setViewName("login");
  }

  @Bean
  @Autowired
  public SAMLMessageHandler samlMessageHandler(@Value("${idp.clock_skew}") int clockSkew,
                                               @Value("${idp.expires}") int expires,
                                               @Value("${idp.base_url}") String idpBaseUrl,
                                               @Value("${idp.compare_endpoints}") boolean compareEndpoints,
                                               IdpConfiguration idpConfiguration,
                                               JKSKeyManager keyManager)
    throws NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, KeyStoreException, IOException, XMLStreamException, XMLParserException, URISyntaxException {
    StaticBasicParserPool parserPool = new StaticBasicParserPool();
    BasicSecurityPolicy securityPolicy = new BasicSecurityPolicy();
    securityPolicy.getPolicyRules().addAll(Arrays.asList(new IssueInstantRule(clockSkew, expires),
      new MessageReplayRule(new ReplayCache(new MapBasedStorageService(), 14400000))));

    HTTPRedirectDeflateDecoder httpRedirectDeflateDecoder = new HTTPRedirectDeflateDecoder(parserPool);
    HTTPPostDecoder httpPostDecoder = new HTTPPostDecoder(parserPool);
    if (!compareEndpoints) {
      URIComparator noopComparator = (uri1, uri2) -> true;
      httpPostDecoder.setURIComparator(noopComparator);
      httpRedirectDeflateDecoder.setURIComparator(noopComparator);
    }

    parserPool.initialize();
    HTTPPostSimpleSignEncoder httpPostSimpleSignEncoder = new HTTPPostSimpleSignEncoder(VelocityFactory.getEngine(), "/templates/saml2-post-simplesign-binding.vm", true);

    return new SAMLMessageHandler(
      keyManager,
      Arrays.asList(httpRedirectDeflateDecoder, httpPostDecoder),
      httpPostSimpleSignEncoder,
      new StaticSecurityPolicyResolver(securityPolicy),
      idpConfiguration,
      idpBaseUrl);
  }

  @Bean
  public static SAMLBootstrap sAMLBootstrap() {
    return new UpgradedSAMLBootstrap();
  }

  @Autowired
  @Bean
  public JKSKeyManager keyManager(@Value("${idp.entity_id}") String idpEntityId,
                                  @Value("${idp.private_key}") String idpPrivateKey,
                                  @Value("${idp.certificate}") String idpCertificate,
                                  @Value("${idp.passphrase}") String idpPassphrase) throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, XMLStreamException {
    KeyStore keyStore = KeyStoreLocator.createKeyStore(idpPassphrase);
    KeyStoreLocator.addPrivateKey(keyStore, idpEntityId, idpPrivateKey, idpCertificate, idpPassphrase);
    return new JKSKeyManager(keyStore, Collections.singletonMap(idpEntityId, idpPassphrase), idpEntityId);
  }

  @Bean
  public ServletContextInitializer servletContextInitializer() {
    //otherwise the two localhost instances override each other session
    return servletContext -> servletContext.getSessionCookieConfig().setName("mujinaIdpSessionId");
  }

  @Configuration
  @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
  protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private IdpConfiguration idpConfiguration;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/", "/metadata", "/favicon.ico", "/api/**", "/*.css").permitAll()
        .antMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().hasRole("USER")
        .and()
        .formLogin()
        .loginPage("/login")
        .permitAll()
        .failureUrl("/login?error=true")
        .permitAll()
        .and()
        .logout()
        .logoutSuccessUrl("/");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(new AuthenticationProvider(idpConfiguration));
    }

  }

}
