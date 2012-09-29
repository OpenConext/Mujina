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

package nl.surfnet.mujina.controllers;

import nl.surfnet.mujina.model.AssertionConsumerServiceURL;
import nl.surfnet.mujina.model.ProtocolBinding;
import nl.surfnet.mujina.model.SSOServiceURL;
import nl.surfnet.mujina.model.SpConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ServiceProviderAPI {

  private final static Logger log = LoggerFactory.getLogger(ServiceProviderAPI.class);

  final SpConfiguration configuration;

  @Autowired
  public ServiceProviderAPI(final SpConfiguration configuration) {
    this.configuration = configuration;
  }

  @RequestMapping(value = { "/ssoServiceURL" }, method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void setSsoServiceURL(@RequestBody SSOServiceURL ssoServiceURL) {
    log.debug("Request to set ssoServiceURL to {}", ssoServiceURL.getValue());
    configuration.setSingleSignOnServiceURL(ssoServiceURL.getValue());
  }

  @RequestMapping(value = { "/protocolBinding" }, method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void setProtocolBinding(@RequestBody ProtocolBinding protocolBinding) {
    log.debug("Request to set protocolBinding to {}", protocolBinding.getValue());
    configuration.setProtocolBinding(protocolBinding.getValue());
  }

  @RequestMapping(value = { "/assertionConsumerServiceURL" }, method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void setAssertionConsumerServiceURL(@RequestBody AssertionConsumerServiceURL assertionConsumerServiceURL) {
    log.debug("Request to set assertionConsumerServiceURL to {}", assertionConsumerServiceURL.getValue());
    configuration.setAssertionConsumerServiceURL(assertionConsumerServiceURL.getValue());
  }

}
