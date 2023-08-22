package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.saml.KeyStoreLocator;
import mujina.saml.UpgradedSAMLBootstrap;
import org.opensaml.common.binding.decoding.URIComparator;
import org.opensaml.common.binding.security.IssueInstantRule;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostSimpleSignEncoder;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.StaticSecurityPolicyResolver;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.SessionCookieConfig;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
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
public class WebSecurityConfigurer implements WebMvcConfigurer {

    @Value("${secure_cookie}")
    private boolean secureCookie;

    @Bean
    @Autowired
    public SAMLMessageHandler samlMessageHandler(@Value("${idp.clock_skew}") int clockSkew,
                                                 @Value("${idp.expires}") int expires,
                                                 @Value("${idp.base_url}") String idpBaseUrl,
                                                 @Value("${idp.compare_endpoints}") boolean compareEndpoints,
                                                 IdpConfiguration idpConfiguration,
                                                 JKSKeyManager keyManager)
            throws XMLParserException, URISyntaxException {
        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        BasicSecurityPolicy securityPolicy = new BasicSecurityPolicy();
        securityPolicy.getPolicyRules().addAll(Arrays.asList(new IssueInstantRule(clockSkew, expires)));

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
        return servletContext -> {
            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setName("mujinaIdpSessionId");
            sessionCookieConfig.setSecure(this.secureCookie);
            sessionCookieConfig.setHttpOnly(true);
        };
    }

    @Configuration
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Autowired
        private IdpConfiguration idpConfiguration;

        @Autowired
        private SAMLMessageHandler samlMessageHandler;

        private SAMLAttributeAuthenticationFilter authenticationFilter() throws Exception {
            SAMLAttributeAuthenticationFilter filter = new SAMLAttributeAuthenticationFilter();
            filter.setAuthenticationManager(authenticationManagerBean());
            filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error=true"));
            return filter;
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            super.configure(web);
            web.ignoring().antMatchers("/internal/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(new ForceAuthnFilter(samlMessageHandler), SAMLAttributeAuthenticationFilter.class)
                    .authorizeRequests()
                    .antMatchers("/", "/metadata", "/favicon.ico", "/api/**", "/*.css", "/*.js").permitAll()
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
        public void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(new AuthenticationProvider(idpConfiguration));
        }

        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    }

}
