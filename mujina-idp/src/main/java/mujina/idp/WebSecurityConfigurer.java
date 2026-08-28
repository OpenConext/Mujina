package mujina.idp;

import jakarta.servlet.SessionCookieConfig;
import mujina.api.IdpConfiguration;
import mujina.saml.KeyStoreLocator;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.security.KeyStore;
import java.time.Clock;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer {

    @Value("${secure_cookie}")
    private boolean secureCookie;

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public SAMLMessageHandler samlMessageHandler(BasicParserPool samlParserPool,
                                                 IdpConfiguration idpConfiguration,
                                                 @Value("${idp.clock_skew}") int clockSkew,
                                                 @Value("${idp.expires}") int expires,
                                                 @Value("${idp.compare_endpoints}") boolean compareEndpoints,
                                                 @Value("${idp.base_url}") String idpBaseUrl) {
        return new SAMLMessageHandler(samlParserPool, idpConfiguration, clockSkew, expires, compareEndpoints, idpBaseUrl);
    }

    @Bean
    public KeyStore idpKeyStore(@Value("${idp.entity_id}") String idpEntityId,
                                @Value("${idp.private_key}") String idpPrivateKey,
                                @Value("${idp.certificate}") String idpCertificate,
                                @Value("${idp.passphrase}") String idpPassphrase) throws Exception {
        KeyStore keyStore = KeyStoreLocator.createKeyStore(idpPassphrase);
        KeyStoreLocator.addPrivateKey(keyStore, idpEntityId, idpPrivateKey, idpCertificate, idpPassphrase);
        return keyStore;
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

    @Bean
    public AuthenticationManager authenticationManager(IdpConfiguration idpConfiguration) {
        return new ProviderManager(new AuthenticationProvider(idpConfiguration));
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/internal/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager,
                                           SAMLMessageHandler samlMessageHandler) throws Exception {
        SAMLAttributeAuthenticationFilter authenticationFilter = new SAMLAttributeAuthenticationFilter();
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error=true"));
        //Filters built manually (not via the formLogin() DSL) default to a request-scoped-only
        //SecurityContextRepository, so the authenticated session never persists without this.
        authenticationFilter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        http
                .authenticationManager(authenticationManager)
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ForceAuthnFilter(samlMessageHandler), SAMLAttributeAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/metadata", "/favicon.ico", "/api/**", "/*.css", "/*.js")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().hasRole("USER"))
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .failureUrl("/login?error=true"))
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }

}
