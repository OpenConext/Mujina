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

import nl.surfnet.mujina.model.CommonConfiguration;
import nl.surfnet.mujina.model.Credential;
import nl.surfnet.mujina.model.EntityID;

@Controller
public class CommonAPI {

    private final static Logger log = LoggerFactory.getLogger(CommonAPI.class);

    private CommonConfiguration configuration;

    @Autowired
    public CommonAPI(final CommonConfiguration configuration) {
        this.configuration = configuration;
    }

    @RequestMapping(value = {"/reset"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void reset() {
        log.debug("Resetting to default configuration");
        configuration.reset();
    }

    @RequestMapping(value = {"/entityid"}, method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setEntityID(@RequestBody EntityID entityID) {
        log.debug("Request to set entityID {}", entityID.getValue());
        configuration.setEntityID(entityID.getValue());
    }

    @RequestMapping(value = {"/signing-credential"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setSigningCredential(@RequestBody Credential credential) {
        log.debug("Request to set signing credential");
        configuration.injectCredential(credential.getCertificate(), credential.getKey());
    }


}
