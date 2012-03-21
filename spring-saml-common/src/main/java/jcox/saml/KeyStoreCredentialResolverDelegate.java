/*
*   Copyright 2010 James Cox <james.s.cox@gmail.com>
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package jcox.saml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * Trivial implementation of CredentialResolver.  Not recommended for production use as
 * it is not secure.
 * 
 * This class loads a Java keystore from spring config and instantiates 
 * an Open SAML KeyStoreCredentialResolver.  All calls are then delegated to the
 * KeyStoreCredentialResolver.
 * 
 * 
 * @author jcox
 *
 */
public class KeyStoreCredentialResolverDelegate implements CredentialResolver, InitializingBean  {

	
	private KeyStoreCredentialResolver  keyStoreCredentialResolver;
	
	private String keystorePassword;
	private Map<String,String> privateKeyPasswordsByAlias;

	@Required
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	@Required
	public void setPrivateKeyPasswordsByAlias(
			Map<String, String> privateKeyPasswordsByAlias) {
		this.privateKeyPasswordsByAlias = privateKeyPasswordsByAlias;
	}

	@Override
	public Iterable<Credential> resolve(CriteriaSet criteriaSet)
			throws SecurityException {
		return keyStoreCredentialResolver.resolve(criteriaSet);
	}

	@Override
	public Credential resolveSingle(CriteriaSet criteriaSet) throws SecurityException {
		return keyStoreCredentialResolver.resolveSingle(criteriaSet);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, keystorePassword.toCharArray()); // otherwise you get an uninitialized keystore exception
		appendToKeyStore(keyStore, "idp", "idp-crt.pem", "idp-key.pkcs8.der", "secret".toCharArray());
        appendToKeyStore(keyStore, "sp", "idp-crt.pem", "idp-key.pkcs8.der", "secret".toCharArray());
        keyStoreCredentialResolver = new KeyStoreCredentialResolver(keyStore, privateKeyPasswordsByAlias);
	}

    /**
     * Append a certificate and private key to a keystore.
     *
     * @param keyStore where to append the certificate and private key to
     * @param keyAlias the alias of the key
     * @param certificateFile the file containing the certificate in the PEM format
     * @param privatekeyFile the file containing the private key in the DER format
     * @param password the password on the key
     *
     * Generate your private key:
     * openssl genrsa -out something.key 1024
     *
     * Show the PEM private key:
     * openssl asn1parse -inform pem -dump -i -in something.key
     *
     * Translate the key to pkcs8 DER format:
     * openssl pkcs8 -topk8 -inform PEM -outform DER -in something.key -nocrypt > something.pkcs8.der
     *
     * Show the DER private key:
     * openssl asn1parse -inform der -dump -i -in something.pkcs8.der
     *
     * Generate a certificate request:
     * openssl req -new -key something.key -out something.csr
     *
     * Generate a certificate:
     * openssl x509 -req -days 365 -in something.csr -signkey something.key -out something.crt
     */
    private void appendToKeyStore(KeyStore keyStore, String keyAlias, String certificateFile, String privatekeyFile, char[] password) throws Exception {
        BufferedInputStream bis = null;
        bis = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(certificateFile));
        CertificateFactory certFact = null;
        Certificate cert = null;
        try {
            certFact = CertificateFactory.getInstance("X.509");
            cert = certFact.generateCertificate(bis);
        } catch(CertificateException e) {
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
