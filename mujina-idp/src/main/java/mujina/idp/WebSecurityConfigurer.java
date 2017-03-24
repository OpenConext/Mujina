package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.saml.KeyStoreLocator;
import org.opensaml.common.binding.decoding.URIComparator;
import org.opensaml.common.binding.security.IssueInstantRule;
import org.opensaml.common.binding.security.MessageReplayRule;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostSimpleSignEncoder;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.util.storage.ReplayCache;
import org.opensaml.ws.security.SecurityPolicyResolver;
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
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
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

  @Value("${idp.entity_id}")
  private String idpEntityId;

  @Value("${idp.private_key}")
  private String idpPrivateKey;

  @Value("${idp.certificate}")
  private String idpCertificate;

  @Value("${idp.passphrase}")
  private String idpPassphrase;

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/login").setViewName("login");
  }

  @Bean
  @Autowired
  public SAMLMessageHandler samlMessageHandler(@Value("${idp.clock_skew}") int clockSkew,
                                               @Value("${idp.expires}") int expires)
    throws NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, KeyStoreException, IOException, XMLStreamException, XMLParserException {
    StaticBasicParserPool parserPool = new StaticBasicParserPool();
    BasicSecurityPolicy securityPolicy = new BasicSecurityPolicy();
    securityPolicy.getPolicyRules().addAll(Arrays.asList(new IssueInstantRule(clockSkew, expires),
      new MessageReplayRule(new ReplayCache(new MapBasedStorageService(), 14400000))));

    HTTPRedirectDeflateDecoder httpRedirectDeflateDecoder = new HTTPRedirectDeflateDecoder(parserPool);

    if (environment.acceptsProfiles("test")) {
      //Lenient URI comparision
      httpRedirectDeflateDecoder.setURIComparator((uri1, uri2) -> true);
    }

    parserPool.initialize();
    return new SAMLMessageHandler(
      keyManager(),
      httpRedirectDeflateDecoder,
      new HTTPPostSimpleSignEncoder(VelocityFactory.getEngine(), "/templates/saml2-post-simplesign-binding.vm", true),
      new StaticSecurityPolicyResolver(securityPolicy),
      idpEntityId);
  }

  @Bean
  public static SAMLBootstrap sAMLBootstrap() {
    return new SAMLBootstrap();
  }

  @Bean
  public KeyManager keyManager() throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, XMLStreamException {
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
        .antMatchers("/metadata", "/favicon.ico", "/api/**", "/resources/**").permitAll()
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
        .logoutSuccessUrl("/login");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(new AuthenticationProvider(idpConfiguration));
    }

  }

}
