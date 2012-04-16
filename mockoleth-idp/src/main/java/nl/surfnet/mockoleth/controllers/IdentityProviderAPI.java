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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import nl.surfnet.mockoleth.model.Attribute;
import nl.surfnet.mockoleth.model.IdpConfiguration;
import nl.surfnet.mockoleth.model.SimpleAuthentication;
import nl.surfnet.mockoleth.model.User;

@Controller
public class IdentityProviderAPI {

    private final static Logger log = LoggerFactory.getLogger(IdentityProviderAPI.class);

    final IdpConfiguration configuration;

    @Autowired
    public IdentityProviderAPI(final IdpConfiguration configuration) {
        this.configuration = configuration;
    }

    @RequestMapping(value = {"/attribute"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setAttribute(@RequestBody Attribute attribute) {
        log.info("Request to set attribute {} to {}", attribute.getValue(), attribute.getName());
        configuration.getAttributes().put(attribute.getName(), attribute.getValue());
    }

    @RequestMapping(value = {"/attribute"}, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void removeAttribute(@RequestBody Attribute attribute) {
        log.info("Request to remove attribute {}", attribute.getName());
        configuration.getAttributes().remove(attribute.getName());
    }

    @RequestMapping(value = {"/user"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void addUser(@RequestBody User user) {
        log.info("Request to add user {} with password {}", user.getName(), user.getPassword());
        final List<GrantedAuthority> grants = new ArrayList<GrantedAuthority>();
        final List<String> authorities = user.getAuthorities();
        for (String authority : authorities) {
            grants.add(new GrantedAuthorityImpl(authority));
        }
        SimpleAuthentication auth = new SimpleAuthentication(user.getName(), user.getPassword(), grants);
        configuration.getUsers().add(auth);
    }
}
