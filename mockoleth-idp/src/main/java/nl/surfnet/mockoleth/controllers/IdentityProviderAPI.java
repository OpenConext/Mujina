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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.surfnet.mockoleth.model.Attribute;
import nl.surfnet.mockoleth.model.Credential;
import nl.surfnet.mockoleth.model.EntityID;
import nl.surfnet.mockoleth.model.IdpConfiguration;
import nl.surfnet.mockoleth.model.User;
import nl.surfnet.mockoleth.spring.security.CustomAuthentication;

@Controller
public class IdentityProviderAPI {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(IdentityProviderAPI.class);

    @Autowired
    IdpConfiguration idpConfiguration;

    @RequestMapping(value = {"/set-signing-credential"}, method = RequestMethod.POST)
    @ResponseBody
    public void setSigningCredential(@RequestBody Credential credential) {
        LOGGER.info("Request to set signing credential");
        idpConfiguration.injectCredential(credential.getCertificate(), credential.getKey());
    }

    @RequestMapping(value = {"/set-entityid"}, method = RequestMethod.POST)
    @ResponseBody
    public void setEntityID(@RequestBody EntityID entityID) {
        LOGGER.info("Request to set entityID {}", entityID.getValue());
        idpConfiguration.setEntityID(entityID.getValue());
    }

    @RequestMapping(value = {"/set-attribute"}, method = RequestMethod.POST)
    @ResponseBody
    public void setAttribute(@RequestBody Attribute attribute) {
        LOGGER.info("Request to set attribute {} to {}", attribute.getValue(), attribute.getName());
        idpConfiguration.getAttributes().put(attribute.getName(), attribute.getValue());
    }

    @RequestMapping(value = {"/remove-attribute"}, method = RequestMethod.POST)
    @ResponseBody
    public void removeAttribute(@RequestBody Attribute attribute) {
        LOGGER.info("Request to remove attribute {}", attribute.getName());
        idpConfiguration.getAttributes().remove(attribute.getName());
    }

    @RequestMapping(value = {"/add-user"}, method = RequestMethod.POST)
    @ResponseBody
    public void addUser(@RequestBody User user) {
        LOGGER.info("Request to add user {} with password {}", user.getName(), user.getPassword());
        CustomAuthentication customAuthentication = new CustomAuthentication(user.getName(), user.getPassword());
        final List<String> authorities = user.getAuthorities();
        for (String authority : authorities) {
            customAuthentication.addAuthority(authority);
        }
        idpConfiguration.getUsers().add(customAuthentication);
    }

    @RequestMapping(value = {"/reset"}, method = RequestMethod.POST)
    @ResponseBody
    public void reset() {
        LOGGER.info("Resetting to default configuration");
        idpConfiguration.reset();
    }
}
