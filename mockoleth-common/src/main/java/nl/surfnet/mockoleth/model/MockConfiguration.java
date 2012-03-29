/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.mockoleth.model;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import nl.surfnet.mockoleth.spring.security.CustomAuthentication;

public class MockConfiguration implements Configuration {

    private Map<String, String> attributes = new TreeMap<String, String>();
    private KeyStore keyStore;
    private String keystorePassword = "secret";
    private final static Logger LOGGER = LoggerFactory.getLogger(MockConfiguration.class);
    private Collection<CustomAuthentication> users = new ArrayList<CustomAuthentication>();

    public MockConfiguration() {
        reset();
    }

    @Override
    public void reset() {
        attributes.clear();
        attributes.put("urn:mace:dir:attribute-def:uid", "alle.veenstra");
        attributes.put("urn:mace:dir:attribute-def:cn", "Alle Veenstra");
        attributes.put("urn:mace:dir:attribute-def:givenName", "Alle");
        attributes.put("urn:mace:dir:attribute-def:sn", "Veenstra");
        attributes.put("urn:mace:dir:attribute-def:displayName", "Alle Veenstra");
        attributes.put("urn:mace:dir:attribute-def:mail", "a.veenstra@onehippo.com");
        attributes.put("urn:mace:terena.org:attribute-def:schacHomeOrganization", "test.surfguest.nl");
        attributes.put("urn:mace:dir:attribute-def:eduPersonPrincipalName", "alle.veenstra@SURFguest.nl");
        attributes.put("urn:oid:1.3.6.1.4.1.1076.20.100.10.10.1", "guest");
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, keystorePassword.toCharArray());
            appendToKeyStore(keyStore, "idp", "idp-crt.pem", "idp-key.pkcs8.der", keystorePassword.toCharArray());
            appendToKeyStore(keyStore, "sp", "idp-crt.pem", "idp-key.pkcs8.der", keystorePassword.toCharArray());
        } catch (Exception e) {
            LOGGER.error("Unable to create default keystore", e);
        }
        users.clear();
        final CustomAuthentication user = new CustomAuthentication("admin", "secret");
        user.addAuthority("ROLE_USER");
        user.addAuthority("ROLE_ADMIN");
        users.add(user);
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public KeyStore getKeyStore() {
        return keyStore;
    }

    @Override
    public Collection<CustomAuthentication> getUsers() {
        return users;
    }

    /**
     * Append a certificate and private key to a keystore.
     *
     * @param keyStore        where to append the certificate and private key to
     * @param keyAlias        the alias of the key
     * @param certificateFile the file containing the certificate in the PEM format
     * @param privatekeyFile  the file containing the private key in the DER format
     * @param password        the password on the key
     *                        <p/>
     *                        Generate your private key:
     *                        openssl genrsa -out something.key 1024
     *                        <p/>
     *                        Show the PEM private key:
     *                        openssl asn1parse -inform pem -dump -i -in something.key
     *                        <p/>
     *                        Translate the key to pkcs8 DER format:
     *                        openssl pkcs8 -topk8 -inform PEM -outform DER -in something.key -nocrypt > something.pkcs8.der
     *                        <p/>
     *                        Show the DER private key:
     *                        openssl asn1parse -inform der -dump -i -in something.pkcs8.der
     *                        <p/>
     *                        Generate a certificate request:
     *                        openssl req -new -key something.key -out something.csr
     *                        <p/>
     *                        Generate a certificate:
     *                        openssl x509 -req -days 365 -in something.csr -signkey something.key -out something.crt
     */
    private void appendToKeyStore(KeyStore keyStore, String keyAlias, String certificateFile, String privatekeyFile, char[] password) throws Exception {
        BufferedInputStream bis = null;
        bis = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(certificateFile));
        CertificateFactory certFact = null;
        Certificate cert = null;
        try {
            certFact = CertificateFactory.getInstance("X.509");
            cert = certFact.generateCertificate(bis);
        } catch (CertificateException e) {
            throw new Exception("Could not instantiate cert", e);
        }
        bis.close();
        ArrayList<Certificate> certs = new ArrayList<Certificate>();
        certs.add(cert);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(privatekeyFile);
        byte[] privKeyBytes = IOUtils.toByteArray(inputStream);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        KeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
        RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(ks);

        final Certificate[] certificates = new Certificate[1];
        certificates[0] = certs.get(0);

        keyStore.setKeyEntry(keyAlias, privKey, password, certificates);
    }
}
