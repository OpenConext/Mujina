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

package nl.surfnet.mujina.saml;

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;

import nl.surfnet.mujina.model.CommonConfiguration;


public class KeyStoreCredentialResolverDelegate implements CredentialResolver {

    private CommonConfiguration configuration;

    @Override
    public Iterable<Credential> resolve(CriteriaSet criteriaSet) throws SecurityException {
        return getKeyStoreCredentialResolver().resolve(criteriaSet);
    }

    @Override
    public Credential resolveSingle(CriteriaSet criteriaSet) throws SecurityException {
        return getKeyStoreCredentialResolver().resolveSingle(criteriaSet);
    }

    public KeyStoreCredentialResolver getKeyStoreCredentialResolver() {
        return new KeyStoreCredentialResolver(configuration.getKeyStore(), configuration.getPrivateKeyPasswords());
    }

    public void setConfiguration(final CommonConfiguration configuration) {
        this.configuration = configuration;
    }
}
