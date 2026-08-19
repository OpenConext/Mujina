package mujina.sp;

import jakarta.servlet.SessionCookieConfig;
import mujina.api.SpConfiguration;
import mujina.saml.KeyStoreLocator;
import mujina.saml.SAMLAttribute;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.security.KeyStore;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer {

    @Value("${secure_cookie}")
    private boolean secureCookie;

    @Bean
    public KeyStore spKeyStore(@Value("${sp.entity_id}") String spEntityId,
                               @Value("${sp.private_key}") String spPrivateKey,
                               @Value("${sp.certificate}") String spCertificate,
                               @Value("${sp.passphrase}") String spPassphrase) throws Exception {
        KeyStore keyStore = KeyStoreLocator.createKeyStore(spPassphrase);
        KeyStoreLocator.addPrivateKey(keyStore, spEntityId, spPrivateKey, spCertificate, spPassphrase);
        return keyStore;
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository(
            SpConfiguration spConfiguration,
            @Value("${sp.idp_metadata_url}") String idpMetadataUrl) {
        return new MutableRelyingPartyRegistrationRepository(spConfiguration, idpMetadataUrl);
    }

    @Bean
    public OpenSaml5AuthenticationRequestResolver authenticationRequestResolver(RelyingPartyRegistrationRepository repository) {
        OpenSaml5AuthenticationRequestResolver resolver = new OpenSaml5AuthenticationRequestResolver(repository);
        resolver.setAuthnRequestCustomizer(context -> {
            if ("true".equals(context.getRequest().getParameter("force-authn"))) {
                context.getAuthnRequest().setForceAuthn(true);
            }
        });
        return resolver;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        OpenSaml5AuthenticationProvider provider = new OpenSaml5AuthenticationProvider();
        Converter<OpenSaml5AuthenticationProvider.ResponseToken, Saml2Authentication> defaultConverter =
                OpenSaml5AuthenticationProvider.createDefaultResponseAuthenticationConverter();
        List<GrantedAuthority> userRole = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        provider.setResponseAuthenticationConverter(responseToken -> {
            Saml2Authentication authentication = defaultConverter.convert(responseToken);
            Response response = responseToken.getResponse();
            Assertion assertion = response.getAssertions().get(0);
            NameID nameID = assertion.getSubject().getNameID();
            List<SAMLAttribute> attributes = assertion.getAttributeStatements().stream()
                    .flatMap(statement -> statement.getAttributes().stream())
                    .map(attribute -> new SAMLAttribute(attribute.getName(), attributeValues(attribute)))
                    .toList();
            SamlPrincipal principal = new SamlPrincipal(nameID.getValue(), nameID.getFormat(), attributes);
            return new Saml2Authentication(principal, authentication.getSaml2Response(), userRole);
        });
        return new ProviderManager(provider);
    }

    private List<String> attributeValues(Attribute attribute) {
        return attribute.getAttributeValues().stream()
                .map(value -> value instanceof XSString xsString ? xsString.getValue()
                        : value.getDOM() != null ? value.getDOM().getTextContent() : "")
                .toList();
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        //otherwise the two localhost instances override each other session
        return servletContext -> {
            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setName("mujinaSpSessionId");
            sessionCookieConfig.setSecure(this.secureCookie);
            sessionCookieConfig.setHttpOnly(true);
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/internal/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RelyingPartyRegistrationRepository relyingPartyRegistrationRepository,
                                           OpenSaml5AuthenticationRequestResolver authenticationRequestResolver,
                                           AuthenticationManager authenticationManager,
                                           @Value("${sp.acs_location_path}") String assertionConsumerServiceURLPath) throws Exception {

        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/user.html");

        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error");

        http
                .authenticationManager(authenticationManager)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/metadata", "/favicon.ico", "/*.css", "/sp.js", "/api/**",
                                assertionConsumerServiceURLPath + "/**").permitAll()
                        .anyRequest().hasRole("USER"))
                .saml2Login(saml2 -> saml2
                        .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
                        .loginProcessingUrl(assertionConsumerServiceURLPath)
                        .authenticationRequestResolver(authenticationRequestResolver)
                        .authenticationManager(authenticationManager)
                        .successHandler(successHandler)
                        .failureHandler(failureHandler))
                .saml2Metadata(meta -> meta.metadataUrl("/metadata"))
                .securityContext(context -> context.securityContextRepository(new HttpSessionSecurityContextRepository()))
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }

}
