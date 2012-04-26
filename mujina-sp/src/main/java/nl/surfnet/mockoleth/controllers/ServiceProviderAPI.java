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

package nl.surfnet.mockoleth.controllers;

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

import nl.surfnet.mockoleth.model.SSOServiceURL;
import nl.surfnet.mockoleth.model.SpConfiguration;

@Controller
public class ServiceProviderAPI {

    private final static Logger log = LoggerFactory
            .getLogger(ServiceProviderAPI.class);

    final SpConfiguration configuration;

    @Autowired
    public ServiceProviderAPI(final SpConfiguration configuration) {
        this.configuration = configuration;
    }

    @RequestMapping(value = {"/ssoServiceURL"}, method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setAttribute(@RequestBody SSOServiceURL ssoServiceURL) {
        log.debug("Request to set ssoServiceURL to {}", ssoServiceURL.getValue());
        configuration.setSingleSignOnServiceURL(ssoServiceURL.getValue());
    }

}
