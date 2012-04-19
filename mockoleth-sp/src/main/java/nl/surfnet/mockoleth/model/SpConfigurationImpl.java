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

import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpConfigurationImpl extends CommonConfigurationImpl implements SpConfiguration {

    private final static Logger log = LoggerFactory.getLogger(SpConfigurationImpl.class);
    private final String defaultIdpSSOServiceURL;

    private String idpSSOServiceURL;

    public SpConfigurationImpl(String defaultIdpSSOServiceURL) {
        this.defaultIdpSSOServiceURL = defaultIdpSSOServiceURL;
        reset();
    }

    @Override
    public void reset() {
        entityId = "http://mock-sp";
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, keystorePassword.toCharArray());
            appendToKeyStore(keyStore, "idp", "idp-crt.pem", "idp-key.pkcs8.der", keystorePassword.toCharArray());
            appendToKeyStore(keyStore, "sp", "idp-crt.pem", "idp-key.pkcs8.der", keystorePassword.toCharArray());
            privateKeyPasswords.put("idp", keystorePassword);
            privateKeyPasswords.put("sp", keystorePassword);
            idpSSOServiceURL = defaultIdpSSOServiceURL;
        } catch (Exception e) {
            log.error("Unable to create default keystore", e);
        }
    }

    public void setSingleSignOnServiceURL(String idpSSOServiceURL) {
        this.idpSSOServiceURL = idpSSOServiceURL;
    }

    public String getSingleSignOnServiceURL() {
        return idpSSOServiceURL;
    }
}
