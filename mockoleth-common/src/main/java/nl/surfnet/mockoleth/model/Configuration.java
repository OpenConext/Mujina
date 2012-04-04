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
import java.util.Collection;
import java.util.Map;

import nl.surfnet.mockoleth.spring.security.CustomAuthentication;

public interface Configuration {
    void reset();

    Map<String, String> getAttributes();

    KeyStore getKeyStore();
    
    Collection<CustomAuthentication> getUsers();

    String getEntityID();

    void setEntityID(String value);

    void injectCredential(String certificate, String key);

    Map<String, String> getPrivateKeyPasswords();
}
