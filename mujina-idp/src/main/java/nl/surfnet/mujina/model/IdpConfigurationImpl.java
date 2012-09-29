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

package nl.surfnet.mujina.model;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class IdpConfigurationImpl extends CommonConfigurationImpl implements IdpConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(IdpConfigurationImpl.class);

    private Map<String, String> attributes = new TreeMap<String, String>();
    private Collection<SimpleAuthentication> users = new ArrayList<SimpleAuthentication>();
    private AuthenticationMethod.Method authMethod;

    public IdpConfigurationImpl() {
        reset();
    }

    @Override
    public void reset() {
        authMethod = AuthenticationMethod.Method.USER;
        entityId = "http://mock-idp";
        attributes.clear();
        attributes.put("urn:mace:dir:attribute-def:uid", "john.doe");
        attributes.put("urn:mace:dir:attribute-def:cn", "John Doe");
        attributes.put("urn:mace:dir:attribute-def:givenName", "John");
        attributes.put("urn:mace:dir:attribute-def:sn", "Doe");
        attributes.put("urn:mace:dir:attribute-def:displayName", "John Doe");
        attributes.put("urn:mace:dir:attribute-def:mail", "j.doe@example.com");
        attributes.put("urn:mace:terena.org:attribute-def:schacHomeOrganization", "example.com");
        attributes.put("urn:mace:dir:attribute-def:eduPersonPrincipalName", "j.doe@example.com");
        attributes.put("urn:oid:1.3.6.1.4.1.1076.20.100.10.10.1", "guest");
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, keystorePassword.toCharArray());
            appendToKeyStore(keyStore, "http://mock-idp", "idp-crt.pem", "idp-key.pkcs8.der", keystorePassword.toCharArray());
            privateKeyPasswords.put("http://mock-idp", keystorePassword);
        } catch (Exception e) {
            LOGGER.error("Unable to create default keystore", e);
        }
        users.clear();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        authorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        final SimpleAuthentication admin = new SimpleAuthentication("admin", "secret", authorities);
        users.add(admin);
        authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        final SimpleAuthentication user= new SimpleAuthentication("user", "secret", authorities);
        users.add(user);
        setSigning(false);
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<SimpleAuthentication> getUsers() {
        return users;
    }

    @Override
    public AuthenticationMethod.Method getAuthentication() {
        return authMethod;
    }

    @Override
    public void setAuthentication(final AuthenticationMethod.Method method) {
        this.authMethod = method;
    }

}
