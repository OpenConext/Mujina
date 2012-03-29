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

package nl.surfnet.mockoleth.saml;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class SecurityPolicyDelegateTest {

    //args to class under test
    @Mock
    MessageContext messageContext;


    //collabs
    List<SecurityPolicyRule> securityPolicyRules;
    @Mock
    SecurityPolicyRule rule1;
    @Mock
    SecurityPolicyRule rule2;

    //class under test
    SecurityPolicyDelegate securityPolicyDelegate;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        securityPolicyRules = new ArrayList<SecurityPolicyRule>();
        securityPolicyRules.add(rule1);
        securityPolicyRules.add(rule2);

        securityPolicyDelegate = new SecurityPolicyDelegate(securityPolicyRules);
    }

    @Test
    public void testEvaluate() throws Exception {

        securityPolicyDelegate.evaluate(messageContext);

        verify(rule1).evaluate(messageContext);
        verify(rule2).evaluate(messageContext);

    }

    @Test
    public void testGetPolicyRules() throws Exception {
        assertEquals(securityPolicyRules, securityPolicyDelegate.getPolicyRules());
    }


}
