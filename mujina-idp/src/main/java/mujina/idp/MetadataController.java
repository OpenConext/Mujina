package mujina.idp;

import mujina.api.IdpConfiguration;
import mujina.idp.saml.SAMLBuilder;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.xml.SerializeSupport;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Element;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static mujina.idp.saml.SAMLBuilder.buildSAMLObject;
import static mujina.idp.saml.SAMLBuilder.signAssertion;

@RestController
public class MetadataController {

    @Autowired
    private IdpConfiguration idpConfiguration;

    @Autowired
    private Clock clock;

    @Value("${idp.saml_binding}")
    private String samlBinding;

    @Value("${idp.entity_descriptor_valid_until_millis:#{null}}")
    private Optional<Integer> entityDescriptorValidUntilMillis;

    @Autowired
    @RequestMapping(method = RequestMethod.GET, value = "/metadata", produces = "application/xml")
    public String metadata(@Value("${idp.base_url}") String idpBaseUrl) throws Exception {
        String entityId = idpConfiguration.getEntityId();

        KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(
                idpConfiguration.getKeyStore(),
                Map.of(entityId, idpConfiguration.getKeystorePassword()),
                UsageType.SIGNING);
        Credential credential = resolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId), new UsageCriterion(UsageType.SIGNING)));

        EntityDescriptor entityDescriptor = buildSAMLObject(EntityDescriptor.class);
        entityDescriptor.setEntityID(entityId);
        entityDescriptor.setID(SAMLBuilder.randomSAMLId());
        entityDescriptorValidUntilMillis.ifPresent(
                value -> entityDescriptor.setValidUntil(Instant.now(clock).plusMillis(value)));

        IDPSSODescriptor idpssoDescriptor = buildSAMLObject(IDPSSODescriptor.class);

        NameIDFormat nameIDFormat = buildSAMLObject(NameIDFormat.class);
        nameIDFormat.setURI(NameIDType.PERSISTENT);
        idpssoDescriptor.getNameIDFormats().add(nameIDFormat);

        idpssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        SingleSignOnService singleSignOnService = buildSAMLObject(SingleSignOnService.class);
        singleSignOnService.setLocation(idpBaseUrl + "/SingleSignOnService");
        singleSignOnService.setBinding(samlBinding);

        idpssoDescriptor.getSingleSignOnServices().add(singleSignOnService);

        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();

        KeyDescriptor encKeyDescriptor = buildSAMLObject(KeyDescriptor.class);
        encKeyDescriptor.setUse(UsageType.SIGNING);
        encKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(credential));

        idpssoDescriptor.getKeyDescriptors().add(encKeyDescriptor);

        entityDescriptor.getRoleDescriptors().add(idpssoDescriptor);

        signAssertion(entityDescriptor, credential, idpConfiguration.getSignatureAlgorithm());

        Element element = XMLObjectSupport.marshall(entityDescriptor);
        return SerializeSupport.nodeToString(element);
    }

}
