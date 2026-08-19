package mujina.sp;

import mujina.api.SpConfiguration;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Builds a fresh {@link RelyingPartyRegistration} from the current, runtime-mutable
 * {@link SpConfiguration} on every lookup, so that {@code PUT /api/ssoServiceURL},
 * {@code /api/protocolBinding}, {@code /api/assertionConsumerServiceURL}, {@code /api/entityid}
 * and {@code /api/signing-credential} all take effect on the next login without a restart.
 */
public class MutableRelyingPartyRegistrationRepository
        implements RelyingPartyRegistrationRepository, Iterable<RelyingPartyRegistration> {

    public static final String REGISTRATION_ID = "idp";

    private final SpConfiguration spConfiguration;
    private final String idpMetadataLocation;

    public MutableRelyingPartyRegistrationRepository(SpConfiguration spConfiguration, String idpMetadataLocation) {
        this.spConfiguration = spConfiguration;
        this.idpMetadataLocation = idpMetadataLocation;
    }

    @Override
    public RelyingPartyRegistration findByRegistrationId(String registrationId) {
        if (!REGISTRATION_ID.equals(registrationId)) {
            return null;
        }
        return build();
    }

    @Override
    public Iterator<RelyingPartyRegistration> iterator() {
        return List.of(build()).iterator();
    }

    private RelyingPartyRegistration build() {
        Saml2MessageBinding binding = Saml2MessageBinding.from(spConfiguration.getProtocolBinding());

        return RelyingPartyRegistrations.fromMetadataLocation(idpMetadataLocation)
                .registrationId(REGISTRATION_ID)
                .entityId(spConfiguration.getEntityId())
                .assertionConsumerServiceLocation(spConfiguration.getAssertionConsumerServiceURL())
                .assertionConsumerServiceBinding(binding != null ? binding : Saml2MessageBinding.POST)
                .authnRequestsSigned(spConfiguration.isNeedsSigning())
                .signingX509Credentials(credentials -> {
                    credentials.clear();
                    credentials.add(currentSigningCredential());
                })
                .assertingPartyMetadata(ap -> ap.singleSignOnServiceLocation(spConfiguration.getIdpSSOServiceURL()))
                .build();
    }

    private Saml2X509Credential currentSigningCredential() {
        try {
            KeyStore keyStore = spConfiguration.getKeyStore();
            String alias = spConfiguration.getEntityId();
            KeyStore.PasswordProtection passwordProtection =
                    new KeyStore.PasswordProtection(spConfiguration.getKeystorePassword().toCharArray());
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, passwordProtection);
            PrivateKey privateKey = entry.getPrivateKey();
            X509Certificate certificate = (X509Certificate) entry.getCertificate();
            return Saml2X509Credential.signing(privateKey, certificate);
        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve SP signing credential", e);
        }
    }

}
