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

package nl.surfnet.mockoleth.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RealAuthenticationFailureHandlerTest {

    //args to class under test
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    IdentityProviderAuthenticationException idpException;
    @Mock
    ServiceProviderAuthenticationException spException;
    @Mock
    HttpSession session;

    //collabs
    @Mock
    RequestCache requestCache;

    @Mock
    SavedRequest savedRequest;
    private String redirectURL = "http://sp/protected";

    //class under test
    RealAuthenticationFailureHandler handler;


    @Before
    public void before() {

        MockitoAnnotations.initMocks(this);

        handler = new RealAuthenticationFailureHandler(requestCache);
    }

    @Test
    public void testOnAuthnFailureIdentityProviderException() throws Exception {

        when(requestCache.getRequest(request, response)).thenReturn(savedRequest);
        when(savedRequest.getRedirectUrl()).thenReturn(redirectURL);

        handler.onAuthenticationFailure(request, response, idpException);

        verify(response).sendRedirect(redirectURL);
    }

    @Test
    public void testOnAuthnFailureServiceProviderException() throws Exception {

        when(requestCache.getRequest(request, response)).thenReturn(savedRequest);
        when(savedRequest.getRedirectUrl()).thenReturn(redirectURL);

        handler.onAuthenticationFailure(request, response, spException);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }


}
