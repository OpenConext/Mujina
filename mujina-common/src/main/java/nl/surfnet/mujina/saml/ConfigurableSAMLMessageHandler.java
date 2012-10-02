/*
 Copyright 2012 SURFnet bv, The Netherlands

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package nl.surfnet.mujina.saml;

import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.ws.security.SecurityPolicyResolver;

import nl.surfnet.mujina.model.CommonConfiguration;
import nl.surfnet.spring.security.opensaml.SAMLMessageHandlerImpl;

/**
 * Variant of SAMLMessageHandlerImpl that uses Mujina configuration for entity id and whether to sign.
 */
public class ConfigurableSAMLMessageHandler extends SAMLMessageHandlerImpl {
  private CommonConfiguration configuration;

  public void setConfiguration(CommonConfiguration configuration) {
    this.configuration = configuration;
  }

  public ConfigurableSAMLMessageHandler(SAMLMessageDecoder decoder, SecurityPolicyResolver resolver) {
    super(decoder, resolver);
  }

  @Override
  public String getEntityId() {
    return configuration.getEntityID();
  }

  public boolean needsSigning() {
    return configuration.needsSigning();
  }
}
