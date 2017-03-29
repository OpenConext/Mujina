package mujina.sp;

import mujina.saml.KeyStoreLocator;
import mujina.saml.ProxiedSAMLContextProviderLB;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

  @Value("${sp.idp_metadata_url}")
  private String identityProviderMetadataUrl;

  @Value("${sp.base_url}")
  private String spBaseUrl;

  @Value("${sp.entity_id}")
  private String spEntityId;

  @Value("${sp.private_key}")
  private String spPrivateKey;

  @Value("${sp.certificate}")
  private String spCertificate;

  @Value("${sp.passphrase}")
  private String spPassphrase;

  @Value("${sp.acs_location_path}")
  private String assertionConsumerServiceURLPath;

  private DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();

  @Bean
  public SAMLAuthenticationProvider samlAuthenticationProvider() {
    SAMLAuthenticationProvider samlAuthenticationProvider = new RoleSAMLAuthenticationProvider();
    samlAuthenticationProvider.setUserDetails(new DefaultSAMLUserDetailsService());
    samlAuthenticationProvider.setForcePrincipalAsString(false);
    samlAuthenticationProvider.setExcludeCredential(true);
    return samlAuthenticationProvider;
  }

  @Bean
  public SAMLEntryPoint samlEntryPoint() {
    WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
    webSSOProfileOptions.setIncludeScoping(false);

    SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
    samlEntryPoint.setFilterProcessesUrl("login");
    samlEntryPoint.setDefaultProfileOptions(webSSOProfileOptions);
    return samlEntryPoint;
  }

  @Bean
  public ServletContextInitializer servletContextInitializer() {
    //otherwise the two localhost instances override each other session
    return servletContext -> servletContext.getSessionCookieConfig().setName("mujinaSpSessionId");
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/health", "/info");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
      .antMatchers("/", "/metadata", "/favicon.ico", "/css.*", "/api/**", assertionConsumerServiceURLPath + "/**").permitAll()
      .anyRequest().hasRole("USER")
      .and()
      .httpBasic().authenticationEntryPoint(samlEntryPoint())
      .and()
      .csrf().disable()
      .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
      .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
      .logout()
      .logoutSuccessUrl("/");
  }

  // Handler deciding where to redirect user after successful login
  @Bean
  public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
    SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler =
      new SavedRequestAwareAuthenticationSuccessHandler();
    successRedirectHandler.setDefaultTargetUrl("/user");
    return successRedirectHandler;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(samlAuthenticationProvider());
  }

  @Bean
  public MetadataDisplayFilter metadataDisplayFilter() {
    DefaultMetadataDisplayFilter displayFilter = new DefaultMetadataDisplayFilter();
    displayFilter.setFilterProcessesUrl("metadata");
    return displayFilter;
  }

  @Bean
  public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
    SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
    failureHandler.setUseForward(true);
    failureHandler.setDefaultFailureUrl("/error");
    return failureHandler;
  }

  @Bean
  public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
    SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
    samlWebSSOProcessingFilter.setFilterProcessesUrl("saml/SSO");
    samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
    samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
    samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
    return samlWebSSOProcessingFilter;
  }

  @Bean
  public MetadataGeneratorFilter metadataGeneratorFilter() throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, XMLStreamException {
    return new MetadataGeneratorFilter(metadataGenerator());
  }

  @Bean
  public FilterChainProxy samlFilter() throws Exception {
    List<SecurityFilterChain> chains = new ArrayList<>();
    chains.add(chain("/login/**", samlEntryPoint()));
    chains.add(chain("/metadata/**", metadataDisplayFilter()));
    chains.add(chain(assertionConsumerServiceURLPath + "/**", samlWebSSOProcessingFilter()));
    return new FilterChainProxy(chains);
  }

  private DefaultSecurityFilterChain chain(String pattern, Filter entryPoint) {
    return new DefaultSecurityFilterChain(new AntPathRequestMatcher(pattern), entryPoint);
  }

  @Bean
  public ExtendedMetadata extendedMetadata() {
    ExtendedMetadata extendedMetadata = new ExtendedMetadata();
    extendedMetadata.setIdpDiscoveryEnabled(false);
    extendedMetadata.setSignMetadata(true);
    return extendedMetadata;
  }

  @Bean
  public MetadataProvider identityProvider() throws MetadataProviderException, XMLParserException {
    Resource resource = defaultResourceLoader.getResource(identityProviderMetadataUrl);
    ResourceMetadataProvider resourceMetadataProvider = new ResourceMetadataProvider(resource);
    resourceMetadataProvider.setParserPool(parserPool());
    ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(resourceMetadataProvider, extendedMetadata());
    extendedMetadataDelegate.setMetadataTrustCheck(true);
    extendedMetadataDelegate.setMetadataRequireSignature(true);
    return extendedMetadataDelegate;
  }

  @Bean
  @Qualifier("metadata")
  public CachingMetadataManager metadata() throws MetadataProviderException, XMLParserException {
    List<MetadataProvider> providers = new ArrayList<>();
    providers.add(identityProvider());

    return new CachingMetadataManager(providers);
  }

  @Bean
  public VelocityEngine velocityEngine() {
    return VelocityFactory.getEngine();
  }

  @Bean(initMethod = "initialize")
  public ParserPool parserPool() {
    return new StaticBasicParserPool();
  }

  @Bean(name = "parserPoolHolder")
  public ParserPoolHolder parserPoolHolder() {
    return new ParserPoolHolder();
  }

  @Bean
  public SAMLContextProvider contextProvider() throws URISyntaxException {
    return new ProxiedSAMLContextProviderLB(new URI(spBaseUrl));
  }

  @Bean
  public MetadataGenerator metadataGenerator() throws NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, KeyStoreException, IOException, XMLStreamException {
    MetadataGenerator metadataGenerator = new MetadataGenerator();
    metadataGenerator.setEntityId(spEntityId);
    metadataGenerator.setEntityBaseURL(spBaseUrl);
    metadataGenerator.setExtendedMetadata(extendedMetadata());
    metadataGenerator.setIncludeDiscoveryExtension(false);
    metadataGenerator.setKeyManager(keyManager());
    return metadataGenerator;
  }

  @Bean
  public JKSKeyManager keyManager() throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, XMLStreamException {
    KeyStore keyStore = KeyStoreLocator.createKeyStore(spPassphrase);
    KeyStoreLocator.addPrivateKey(keyStore, spEntityId, spPrivateKey, spCertificate, spPassphrase);
    return new JKSKeyManager(keyStore, Collections.singletonMap(spEntityId, spPassphrase), spEntityId);
  }

}
