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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import nl.surfnet.mujina.model.Attribute;
import nl.surfnet.mujina.model.AuthenticationMethod;
import nl.surfnet.mujina.model.IdpConfiguration;
import nl.surfnet.mujina.model.SimpleAuthentication;
import nl.surfnet.mujina.model.User;

@Controller
public class IdentityProviderAPI {

    private final static Logger log = LoggerFactory.getLogger(IdentityProviderAPI.class);

    final IdpConfiguration configuration;

    @Autowired
    public IdentityProviderAPI(final IdpConfiguration configuration) {
        this.configuration = configuration;
    }

    @RequestMapping(value = {"/attributes/{name}"}, method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setAttribute(@PathVariable String name, @RequestBody Attribute attribute) {
        log.debug("Request to set attribute {} to {}", attribute.getValue(), name);
        configuration.getAttributes().put(name, attribute.getValue());
    }

    @RequestMapping(value = {"/attributes/{name}"}, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void removeAttribute(@PathVariable String name) {
        log.debug("Request to remove attribute {}", name);
        configuration.getAttributes().remove(name);
    }

    @RequestMapping(value = {"/users"}, method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void addUser(@RequestBody User user) {
        log.debug("Request to add user {} with password {}", user.getName(), user.getPassword());
        final List<GrantedAuthority> grants = new ArrayList<GrantedAuthority>();
        final List<String> authorities = user.getAuthorities();
        for (String authority : authorities) {
            grants.add(new GrantedAuthorityImpl(authority));
        }
        SimpleAuthentication auth = new SimpleAuthentication(user.getName(), user.getPassword(), grants);
        configuration.getUsers().add(auth);
    }

    @RequestMapping(value = {"/authmethod"}, method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setAuthenticationMethod(@RequestBody AuthenticationMethod authenticationMethod) {
        log.debug("Request to set auth method to {}", authenticationMethod.getValue());
        final AuthenticationMethod.Method method = AuthenticationMethod.Method.valueOf(authenticationMethod.getValue());
        configuration.setAuthentication(method);
    }
}
