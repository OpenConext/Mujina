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

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.surfnet.spring.security.opensaml.util.KeyStoreUtil;

public abstract class CommonConfigurationImpl implements CommonConfiguration {

  private final static Logger LOGGER = LoggerFactory.getLogger(CommonConfigurationImpl.class);

  protected KeyStore keyStore;
  protected String keystorePassword = "secret";
  private boolean needsSigning=false;
  private String signatureAlgorithm;
  private Endpoint sloEndpoint;
  protected String entityId;

  protected Map<String, String> privateKeyPasswords = new HashMap<String, String>();

  @Override
  public Map<String, String> getPrivateKeyPasswords() {
    return privateKeyPasswords;
  }

  @Override
  public KeyStore getKeyStore() {
    return keyStore;
  }

  public boolean needsSigning() {
    return needsSigning;
  }

  public void setSigning(boolean needsSigning) {
    this.needsSigning = needsSigning;
  }

  @Override
  public String getEntityID() {
    return entityId;
  }

  @Override
  public void setEntityID(final String newEntityId) {
    try {
      final KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(keystorePassword.toCharArray());
      final KeyStore.Entry keyStoreEntry = keyStore.getEntry(this.entityId, passwordProtection);
      keyStore.setEntry(newEntityId, keyStoreEntry, passwordProtection);
      privateKeyPasswords.put(newEntityId, keystorePassword);
    } catch (Exception e) {
      LOGGER.warn("Unable to update signing key in key store", e);
    }
    this.entityId = newEntityId;
  }

  @Override
  public void injectCredential(final String certificate, final String key) {
    try {
      if (keyStore.containsAlias(entityId)) {
        keyStore.deleteEntry(entityId);
      }
      injectKeyStore(entityId, certificate, key);
    } catch (Exception e) {
      throw new RuntimeException("Unable to append signing credential", e);
    }
  }

  private void injectKeyStore(String alias, String pemCert, String pemKey) throws Exception {
    String wrappedCert = "-----BEGIN CERTIFICATE-----\n" + pemCert + "\n-----END CERTIFICATE-----";

    ByteArrayInputStream certificateInputStream = new ByteArrayInputStream(wrappedCert.getBytes());

    byte[] key = Base64.decodeBase64(pemKey);
    KeyStoreUtil.appendKeyToKeyStore(keyStore, alias, certificateInputStream, new ByteArrayInputStream(key), keystorePassword.toCharArray());

  }


  @Override public void setSignatureAlgorithm(String signatureAlgorithm) {
    this.signatureAlgorithm = signatureAlgorithm;
  }

  @Override public String getSignatureAlgorithm() {
    return this.signatureAlgorithm;
  }

  @Override public void setSLOEndpoint(Endpoint sloEndpoint) {
    this.sloEndpoint = sloEndpoint;
  }

  @Override public Endpoint getSLOEndpoint() {
    return sloEndpoint;
  }

}
