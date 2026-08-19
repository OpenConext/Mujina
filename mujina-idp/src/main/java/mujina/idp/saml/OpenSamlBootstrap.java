package mujina.idp.saml;

import net.shibboleth.shared.xml.impl.BasicParserPool;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Configuration
public class OpenSamlBootstrap {

    @Bean
    public BasicParserPool samlParserPool() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.setMaxPoolSize(50);
        parserPool.setCoalescing(true);
        parserPool.setExpandEntityReferences(false);
        parserPool.setIgnoreComments(true);
        parserPool.setIgnoreElementContentWhitespace(true);
        parserPool.setNamespaceAware(true);
        parserPool.setSchema(null);
        parserPool.setDTDValidating(false);
        parserPool.setXincludeAware(false);
        parserPool.setBuilderAttributes(new HashMap<>());

        Map<String, Boolean> parserBuilderFeatures = new HashMap<>();
        parserBuilderFeatures.put("http://apache.org/xml/features/disallow-doctype-decl", TRUE);
        parserBuilderFeatures.put("http://javax.xml.XMLConstants/feature/secure-processing", TRUE);
        parserBuilderFeatures.put("http://xml.org/sax/features/external-general-entities", FALSE);
        parserBuilderFeatures.put("http://apache.org/xml/features/validation/schema/normalized-value", FALSE);
        parserBuilderFeatures.put("http://xml.org/sax/features/external-parameter-entities", FALSE);
        parserBuilderFeatures.put("http://apache.org/xml/features/dom/defer-node-expansion", FALSE);
        parserPool.setBuilderFeatures(parserBuilderFeatures);

        parserPool.initialize();

        InitializationService.initialize();

        synchronized (ConfigurationService.class) {
            XMLObjectProviderRegistry registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
            registry.setParserPool(parserPool);
        }

        return parserPool;
    }
}
