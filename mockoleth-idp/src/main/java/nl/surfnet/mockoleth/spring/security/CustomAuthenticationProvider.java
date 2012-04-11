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

package nl.surfnet.mockoleth.spring.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import nl.surfnet.mockoleth.model.IdpConfiguration;
import nl.surfnet.mockoleth.model.SimpleAuthentication;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private IdpConfiguration idpConfiguration;
    
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String name = authentication.getName();
        final String password = authentication.getCredentials().toString();
        final Collection<SimpleAuthentication> users = idpConfiguration.getUsers();
        for (SimpleAuthentication user : users) {
            if (user.getPrincipal().equals(name) && user.getCredentials().equals(password)) {
                return user;
            }
        }
        throw new AuthenticationException("Can not log in") {};
    }

    @Override
    public boolean supports(final Class method) {
        return method.equals(UsernamePasswordAuthenticationToken.class);
    }
}
