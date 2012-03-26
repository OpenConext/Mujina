/*
*   Copyright 2010 James Cox <james.s.cox@gmail.com>
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package nl.surfnet.mockoleth.saml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jcox.saml.SSOSuccessAuthnResponder;
import jcox.saml.SecurityPolicyDelegate;
import jcox.saml.SingleSignOnService;
import jcox.spring.AuthnRequestInfo;
import jcox.test.AbstractRequestIntTest;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.common.binding.security.IssueInstantRule;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.util.storage.MapBasedStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests both SingleSignOnService and SSOSuccessAuthnResponder.
 * 
 * 
 * Processes SAML AuthnRequests that were generated with SAMLAuthenticationEntryPointIntTest.
 * 
 * 
 * @author jcox
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:applicationContext-property-mappings.xml",
        "classpath:applicationContext-idp-config.xml",
        "classpath:applicationContext-spring-security.xml"})
public class SSOIntTest extends AbstractRequestIntTest {

	private final static Logger logger = LoggerFactory.getLogger(SSOIntTest.class);
	final String userName = "dude@idp.com";
	
	final String role1 = "ROLE_1"; 
	final String role2 = "ROLE_2"; 
	private String clientIP = "10.40.125.1";
	
	//args to class under test 
	@Mock private HttpServletRequest request;
	@Mock private RequestDispatcher requestDispatcher;
	MockHttpServletResponse response;
	
	@Captor ArgumentCaptor<AuthnRequest> captor;
	
	private final DateTime authnInstant = new DateTime();
	@Mock HttpSession session;
	
	@Mock SecurityContext context;
	UsernamePasswordAuthenticationToken authToken;
	UserDetails userDeatils;
	String credentials = "secret";
	Collection<GrantedAuthorityImpl> authorities;
	@Mock WebAuthenticationDetails details;

	
	 public static final String SAML_REQUEST_POST_PARAM ="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOkF1dGhuUmVxdWVzdCB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJodHRwczovL2xvY2FsaG9zdDo4MDgwL3NwL0Fzc2VydGlvbkNvbnN1bWVyU2VydmljZSIgRGVzdGluYXRpb249Imh0dHBzOi8vbG9jYWxob3N0OjgwODAvaWRwL1NTT1NlcnZpY2UiIElEPSJlZDg2MjgzYy0zYWU5LTQwYzktOTE1OS0wYzBlNzEwNDc5MGIiIElzc3VlSW5zdGFudD0iMjAxMC0xMS0yM1QyMTozNDoxMi4wOTJaIiBWZXJzaW9uPSIyLjAiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5Ij5zcDwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgo8ZHM6U2lnbmVkSW5mbz4KPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNyc2Etc2hhMSIvPgo8ZHM6UmVmZXJlbmNlIFVSST0iI2VkODYyODNjLTNhZTktNDBjOS05MTU5LTBjMGU3MTA0NzkwYiI+CjxkczpUcmFuc2Zvcm1zPgo8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz4KPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyI+PGVjOkluY2x1c2l2ZU5hbWVzcGFjZXMgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIgUHJlZml4TGlzdD0iZHMgc2FtbDIgc2FtbDJwIi8+PC9kczpUcmFuc2Zvcm0+CjwvZHM6VHJhbnNmb3Jtcz4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+CjxkczpEaWdlc3RWYWx1ZT5xem1zcWpiVmVnWm0xWWszMGZXK080aGp2NVE9PC9kczpEaWdlc3RWYWx1ZT4KPC9kczpSZWZlcmVuY2U+CjwvZHM6U2lnbmVkSW5mbz4KPGRzOlNpZ25hdHVyZVZhbHVlPgpTMjgrdmhlai9NTFBOMHgvWi80TXpZbTExc1FFZ1pwbDl0czJuaE9FN2luU2todytZdE40bWRVbHUxL3A5YTIybzlGM0tGZGFsVUlVCklVaXlaRU52eGVSd3FRZzRTNnZ3NnEyQXl2dm1vL0tFSHVnZUg4Nloxbksyd0prZkNMb0hYOHZocWUyWmcyN2hXRlNqQkREVCtNekUKbHM1RTRwdGNwaExRRmI0UkhHWT0KPC9kczpTaWduYXR1cmVWYWx1ZT4KPGRzOktleUluZm8+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJQ1JqQ0NBYThDQkV5eHhOWXdEUVlKS29aSWh2Y05BUUVGQlFBd2FqRWtNQ0lHQ1NxR1NJYjNEUUVKQVJZVmFtRnRaWE11Y3k1agpiM2hBWjIxaGFXd3VZMjl0TVFzd0NRWURWUVFHRXdKVlV6RVpNQmNHQTFVRUNnd1FVMlZ5ZG1salpTQlFjbTkyYVdSbGNqRUxNQWtHCkExVUVDd3dDYzNBeERUQUxCZ05WQkFNTUJHcGpiM2d3SGhjTk1UQXhNREV3TVRNMU1URTRXaGNOTWpBeE1EQTNNVE0xTVRFNFdqQnEKTVNRd0lnWUpLb1pJaHZjTkFRa0JGaFZxWVcxbGN5NXpMbU52ZUVCbmJXRnBiQzVqYjIweEN6QUpCZ05WQkFZVEFsVlRNUmt3RndZRApWUVFLREJCVFpYSjJhV05sSUZCeWIzWnBaR1Z5TVFzd0NRWURWUVFMREFKemNERU5NQXNHQTFVRUF3d0VhbU52ZURDQm56QU5CZ2txCmhraUc5dzBCQVFFRkFBT0JqUUF3Z1lrQ2dZRUFpKzBBVy9tbFlEcWhHVXZQOG1FUWJWanY3cFp5OXpacFRpc0FmZzkxYVdhdmxpU2sKRCtBUjM4bmJrQ0tWVWdUNXp5ZERBak9mbXNPQWtVYWdGK00zK05TUXBZdHdIbU5rTHhJWE1USG5POGFkSDNCYUNkc0V2Nk5aQk5xWAp3elhTS1dGdEYrU1ZndW1USk92QmhaQjJrT1l5cFdzMzNCczJCVGFLWk9LR2lnMENBd0VBQVRBTkJna3Foa2lHOXcwQkFRVUZBQU9CCmdRQjNDZmUwaVRmclhZOUUyMlRGeTViODdrd3BES2pMb3BOTHRYM2txU1VsZmpuYk41dFlONHpyOTFINWRaVWt1RkY4M3o3enR6S2kKemtjeGlNZ1ZWUWtVMlgxYm41U2RFcnZtUzdhRWNHOCs1VGRsTzViZis4YXMwNHU1cXVnK29RdW41czF0OW1TdmFGN09sNUNYL2drcApFVVRqWHgyOGtsZGJZN0VUZ0RVclN3PT08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48L3NhbWwycDpBdXRoblJlcXVlc3Q+";                
	 public static final String SIGNATURE_POST_PARAM ="Dc+6OIBYFRZYDlvnpf/mu/NmKddJBaVKE5tiacSQRqkffNQSADSLuShVmEAK3M4BMQw4sw8/8MMjv7JAiYLC4r+DviNs5idWfxgFb2MEufXy60JP7Qjck2ZYsIGkpMl87hYvLIVeNpQBJJxenYcPvIXDXnKlcusg6AWuVl7vLJQ=";                
	 public static final String SIG_ALG_POST_PARAM="http://www.w3.org/2000/09/xmldsig#rsa-sha1";               
	 public static final String KEY_INFO_POST_PARAM="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48ZHM6S2V5SW5mbyB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJQ1JqQ0NBYThDQkV5eHhOWXdEUVlKS29aSWh2Y05BUUVGQlFBd2FqRWtNQ0lHQ1NxR1NJYjNEUUVKQVJZVmFtRnRaWE11Y3k1agpiM2hBWjIxaGFXd3VZMjl0TVFzd0NRWURWUVFHRXdKVlV6RVpNQmNHQTFVRUNnd1FVMlZ5ZG1salpTQlFjbTkyYVdSbGNqRUxNQWtHCkExVUVDd3dDYzNBeERUQUxCZ05WQkFNTUJHcGpiM2d3SGhjTk1UQXhNREV3TVRNMU1URTRXaGNOTWpBeE1EQTNNVE0xTVRFNFdqQnEKTVNRd0lnWUpLb1pJaHZjTkFRa0JGaFZxWVcxbGN5NXpMbU52ZUVCbmJXRnBiQzVqYjIweEN6QUpCZ05WQkFZVEFsVlRNUmt3RndZRApWUVFLREJCVFpYSjJhV05sSUZCeWIzWnBaR1Z5TVFzd0NRWURWUVFMREFKemNERU5NQXNHQTFVRUF3d0VhbU52ZURDQm56QU5CZ2txCmhraUc5dzBCQVFFRkFBT0JqUUF3Z1lrQ2dZRUFpKzBBVy9tbFlEcWhHVXZQOG1FUWJWanY3cFp5OXpacFRpc0FmZzkxYVdhdmxpU2sKRCtBUjM4bmJrQ0tWVWdUNXp5ZERBak9mbXNPQWtVYWdGK00zK05TUXBZdHdIbU5rTHhJWE1USG5POGFkSDNCYUNkc0V2Nk5aQk5xWAp3elhTS1dGdEYrU1ZndW1USk92QmhaQjJrT1l5cFdzMzNCczJCVGFLWk9LR2lnMENBd0VBQVRBTkJna3Foa2lHOXcwQkFRVUZBQU9CCmdRQjNDZmUwaVRmclhZOUUyMlRGeTViODdrd3BES2pMb3BOTHRYM2txU1VsZmpuYk41dFlONHpyOTFINWRaVWt1RkY4M3o3enR6S2kKemtjeGlNZ1ZWUWtVMlgxYm41U2RFcnZtUzdhRWNHOCs1VGRsTzViZis4YXMwNHU1cXVnK29RdW41czF0OW1TdmFGN09sNUNYL2drcApFVVRqWHgyOGtsZGJZN0VUZ0RVclN3PT08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz4=";                

	 
	 //generated with a different key, entity id is still sp
	 public static final String SAML_REQUEST_WITH_BAD_SIG_POST_PARAM ="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOkF1dGhuUmVxdWVzdCB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJodHRwczovL2xvY2FsaG9zdDo4MDgwL3NwL0Fzc2VydGlvbkNvbnN1bWVyU2VydmljZSIgRGVzdGluYXRpb249Imh0dHBzOi8vbG9jYWxob3N0OjgwODAvaWRwL1NTT1NlcnZpY2UiIElEPSI3YWRkNTg3ZC1jZDQ4LTQ3YzUtOWU2NC1mMWU2YjFkYTM4MmMiIElzc3VlSW5zdGFudD0iMjAxMC0xMi0wNFQxNzo0Nzo1Mi4yNDFaIiBWZXJzaW9uPSIyLjAiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5Ij5zcDwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgo8ZHM6U2lnbmVkSW5mbz4KPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNyc2Etc2hhMSIvPgo8ZHM6UmVmZXJlbmNlIFVSST0iIzdhZGQ1ODdkLWNkNDgtNDdjNS05ZTY0LWYxZTZiMWRhMzgyYyI+CjxkczpUcmFuc2Zvcm1zPgo8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz4KPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyI+PGVjOkluY2x1c2l2ZU5hbWVzcGFjZXMgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIgUHJlZml4TGlzdD0iZHMgc2FtbDIgc2FtbDJwIi8+PC9kczpUcmFuc2Zvcm0+CjwvZHM6VHJhbnNmb3Jtcz4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+CjxkczpEaWdlc3RWYWx1ZT5EU3UzNnhlTzJlT1BoSTJDMDdNMHdjZ05XRzg9PC9kczpEaWdlc3RWYWx1ZT4KPC9kczpSZWZlcmVuY2U+CjwvZHM6U2lnbmVkSW5mbz4KPGRzOlNpZ25hdHVyZVZhbHVlPgpnRkQyVlJCYXpveGVQUHlqTVN0d0pCcDFxN2NGNlF5WDBLWGlLd2FvUk1YWU1OeUZWQzg3WGJHWHV3aXVEbS9BUnBEZFkzSDVVaGVJClJoYXk2NTJpY05lUGp5MmMzTGpQcW44cjlDVW5nZWc1NmNVUlhZTjE4a1oxTzVablpRbXhwVVJtVmRtaUNOcDVxMXlvZkdiQUxLci8KRVZTdVZ0Wm44eXF5QzFpODFvZz0KPC9kczpTaWduYXR1cmVWYWx1ZT4KPGRzOktleUluZm8+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJQ2NEQ0NBZGtDQkV6NmZaWXdEUVlKS29aSWh2Y05BUUVMQlFBd2Z6RWdNQjRHQ1NxR1NJYjNEUUVKQVJZUmIzUm9aWEpBYm05MwphR1Z5WlM1dmNtY3hDekFKQmdOVkJBWVRBbFZUTVE0d0RBWURWUVFJREFWdmRHaGxjakVPTUF3R0ExVUVCd3dGYjNSb1pYSXhEakFNCkJnTlZCQW9NQlc5MGFHVnlNUTR3REFZRFZRUUxEQVZ2ZEdobGNqRU9NQXdHQTFVRUF3d0ZiM1JvWlhJd0hoY05NVEF4TWpBME1UYzAKTWpRMldoY05NakF4TWpBeE1UYzBNalEyV2pCL01TQXdIZ1lKS29aSWh2Y05BUWtCRmhGdmRHaGxja0J1YjNkb1pYSmxMbTl5WnpFTApNQWtHQTFVRUJoTUNWVk14RGpBTUJnTlZCQWdNQlc5MGFHVnlNUTR3REFZRFZRUUhEQVZ2ZEdobGNqRU9NQXdHQTFVRUNnd0ZiM1JvClpYSXhEakFNQmdOVkJBc01CVzkwYUdWeU1RNHdEQVlEVlFRRERBVnZkR2hsY2pDQm56QU5CZ2txaGtpRzl3MEJBUUVGQUFPQmpRQXcKZ1lrQ2dZRUFvOXo5NldmNnMwS0ZlTzZ4TkYxRTVKbUxVYUhVUVNrSzlVR1dXYnRjWVoyL0NpK1hNT09Ga29lQVlJT0lsaC81VHE1ZApmV2JlN2w0eEhvZEQ2M2JUbUJIYmVWRWhrR3FZUUh2QVM2TG1ZdzdhTlNBQWpWeHlYMjRBN0wvYjkzYmFBS09xU1EyY1puU1RNQ2xUCmYyK3VETTY3SzBnNXNHK3puL2tnUitTbEFPRUNBd0VBQVRBTkJna3Foa2lHOXcwQkFRc0ZBQU9CZ1FDR1JMU2JTSEM4TjE4OUdRVFoKOGc3YXlleWtzVU5PQmtSOFI3SFg5Z1hiWDJHV25SVHFZdDlUZFZEYUk2Wk5BQ0lOaG9mejBOMWVCbk1RV3NmVnZDblkzbExZRXVoMAppUklkVHB2c2FXc1RGVmt3TGk0b3piZlZqd1doMmQvU01FYlcwalRqY3RMUmRFbUFkYksreUhxZUpIelAwczYxQXJUc1hIQ0V4SFhKCmJRPT08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48L3NhbWwycDpBdXRoblJlcXVlc3Q+";
	 public static final String SIGNATURE_WITH_BAD_SIG_POST_PARAM ="DjFXCeSyEgeCpY08PqxwBT2z0mxrRoX7oVad4Ruy0MH7NoLmoM9iqhH6qINRXEl/17lLijo5cwESPPCW7NSY6ic5jOMGXzm3bikUsG9gGxLxG9tjDQGYeC+P5VpmXGu+sxSXtBFNzRAKVuSLfUEuagZSU1W7tMWDK9KyRJ4tgKA=";                
	 public static final String SIG_ALG_OF_BAD_SIG_POST_PARAM="http://www.w3.org/2000/09/xmldsig#rsa-sha1";               
	 public static final String KEY_INFO_OF_BAD_SIG_POST_PARAM="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48ZHM6S2V5SW5mbyB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJQ2NEQ0NBZGtDQkV6NmZaWXdEUVlKS29aSWh2Y05BUUVMQlFBd2Z6RWdNQjRHQ1NxR1NJYjNEUUVKQVJZUmIzUm9aWEpBYm05MwphR1Z5WlM1dmNtY3hDekFKQmdOVkJBWVRBbFZUTVE0d0RBWURWUVFJREFWdmRHaGxjakVPTUF3R0ExVUVCd3dGYjNSb1pYSXhEakFNCkJnTlZCQW9NQlc5MGFHVnlNUTR3REFZRFZRUUxEQVZ2ZEdobGNqRU9NQXdHQTFVRUF3d0ZiM1JvWlhJd0hoY05NVEF4TWpBME1UYzAKTWpRMldoY05NakF4TWpBeE1UYzBNalEyV2pCL01TQXdIZ1lKS29aSWh2Y05BUWtCRmhGdmRHaGxja0J1YjNkb1pYSmxMbTl5WnpFTApNQWtHQTFVRUJoTUNWVk14RGpBTUJnTlZCQWdNQlc5MGFHVnlNUTR3REFZRFZRUUhEQVZ2ZEdobGNqRU9NQXdHQTFVRUNnd0ZiM1JvClpYSXhEakFNQmdOVkJBc01CVzkwYUdWeU1RNHdEQVlEVlFRRERBVnZkR2hsY2pDQm56QU5CZ2txaGtpRzl3MEJBUUVGQUFPQmpRQXcKZ1lrQ2dZRUFvOXo5NldmNnMwS0ZlTzZ4TkYxRTVKbUxVYUhVUVNrSzlVR1dXYnRjWVoyL0NpK1hNT09Ga29lQVlJT0lsaC81VHE1ZApmV2JlN2w0eEhvZEQ2M2JUbUJIYmVWRWhrR3FZUUh2QVM2TG1ZdzdhTlNBQWpWeHlYMjRBN0wvYjkzYmFBS09xU1EyY1puU1RNQ2xUCmYyK3VETTY3SzBnNXNHK3puL2tnUitTbEFPRUNBd0VBQVRBTkJna3Foa2lHOXcwQkFRc0ZBQU9CZ1FDR1JMU2JTSEM4TjE4OUdRVFoKOGc3YXlleWtzVU5PQmtSOFI3SFg5Z1hiWDJHV25SVHFZdDlUZFZEYUk2Wk5BQ0lOaG9mejBOMWVCbk1RV3NmVnZDblkzbExZRXVoMAppUklkVHB2c2FXc1RGVmt3TGk0b3piZlZqd1doMmQvU01FYlcwalRqY3RMUmRFbUFkYksreUhxZUpIelAwczYxQXJUc1hIQ0V4SFhKCmJRPT08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz4=";
	 

	 private final String ssoServiceURL = "https://localhost:8080/idp/SSOService";
	
	//classes under test
	private SingleSignOnService singleSignOnService;
	private SSOSuccessAuthnResponder authnResponder;
	
	//handle to replay message map
	private MapBasedStorageService<String, ?> mapBasedStorageService;
	//handle to turn off time expiry
	private IssueInstantRule issueInstantRule;
	private SecurityPolicyDelegate securityPolicyDelegate;
	
	@Autowired
	public void setSingleSignOnService(SingleSignOnService singleSignOnService) {
		this.singleSignOnService = singleSignOnService;
	}

	@Autowired
	public void setAuthnResponder(SSOSuccessAuthnResponder authnResponder) {
		this.authnResponder = authnResponder;
	}
	
	@Autowired
	public void setSecurityPolicyDelegate(
			SecurityPolicyDelegate securityPolicyDelegate) {
		this.securityPolicyDelegate = securityPolicyDelegate;
	}

	@Autowired
	public void setMapBasedStorageService(
			MapBasedStorageService mapBasedStorageService) {
		this.mapBasedStorageService = mapBasedStorageService;
	}

	@Autowired
	public void setIssueInstantRule(IssueInstantRule issueInstantRule) {
		this.issueInstantRule = issueInstantRule;
	}

	@Before
	public void before() throws Exception {
	
		MockitoAnnotations.initMocks(this);
		response = new MockHttpServletResponse();
		
		authorities = new HashSet<GrantedAuthorityImpl>();
		authorities.add(new GrantedAuthorityImpl(role1));
		authorities.add(new GrantedAuthorityImpl(role2));
		
		userDeatils = new User(userName,credentials,true, true, true, true, authorities);
		authToken = new UsernamePasswordAuthenticationToken(userDeatils,credentials,authorities);
		
		authToken.setDetails(details);
		
		 SecurityContextHolder.setContext(context);

	}
	
	
	@After
	public void after() throws Exception {
		
		for (Iterator<String> partitions = mapBasedStorageService.getPartitions(); partitions.hasNext();) {
			
			String partition =  partitions.next();
			logger.debug("Removing message cache for issuer {}", partition);
			
				for (Iterator keys = mapBasedStorageService.getKeys(partition); keys.hasNext();) {
					String key = (String) keys.next();
					logger.debug("Removing message with key of {}" , key );
					Object removed = mapBasedStorageService.remove(partition, key);
					logger.debug("Message removed:  {}", removed);
				}	
		}
		
		 
		 if(!securityPolicyDelegate.getPolicyRules().contains(issueInstantRule)){
			 //add it back
			 securityPolicyDelegate.getPolicyRules().add(issueInstantRule);
		 }
		
	}
	

	@Test
	public void testValidSignatureAuthnRequest() throws Exception {
		
		securityPolicyDelegate.getPolicyRules().remove(issueInstantRule);
		
		populateRequestURL(request,ssoServiceURL);
		when(request.getMethod()).thenReturn("POST");		
		when(request.getParameter("SAMLRequest")).thenReturn(SAML_REQUEST_POST_PARAM);
		when(request.getParameter("Signature")).thenReturn(SIGNATURE_POST_PARAM);
		when(request.getParameter("SigAlg")).thenReturn(SIG_ALG_POST_PARAM);
		when(request.getParameter("KeyInfo")).thenReturn(KEY_INFO_POST_PARAM);
		when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
		when(request.getSession()).thenReturn(session);
		when(session.getCreationTime()).thenReturn(authnInstant.getMillis());
		
		singleSignOnService.handleRequest(request, response);
		
		verify(session).setAttribute(anyString(), captor.capture());
		when(context.getAuthentication()).thenReturn(authToken);
		when(session.getAttribute(AuthnRequestInfo.class.getName())).thenReturn(captor.getValue());
		when(details.getRemoteAddress()).thenReturn(clientIP );

		authnResponder.handleRequest(request, response);
		
		assertNotNull("response.getContentAsString was null", response.getContentAsString());
		logger.debug("The response was: {}", response.getContentAsString());
		assertTrue("The response did not contain a SAMLResponse", response.getContentAsString().contains("SAMLResponse"));

	}	
	
	@Test
	public void testOutdatedMessage() throws Exception {

		populateRequestURL(request,ssoServiceURL);
		when(request.getMethod()).thenReturn("POST");		
		when(request.getParameter("SAMLRequest")).thenReturn(SAML_REQUEST_POST_PARAM);
		when(request.getParameter("Signature")).thenReturn(SIGNATURE_POST_PARAM);
		when(request.getParameter("SigAlg")).thenReturn(SIG_ALG_POST_PARAM);
		when(request.getParameter("KeyInfo")).thenReturn(KEY_INFO_POST_PARAM);
		when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
		when(request.getSession()).thenReturn(session);
		when(session.getCreationTime()).thenReturn(authnInstant.getMillis());
		
		singleSignOnService.handleRequest(request, response);
		
		verify(requestDispatcher, never()).forward(request, response);

	}
	
	@Test
	public void testMessageReplay() throws Exception {
		
		securityPolicyDelegate.getPolicyRules().remove(issueInstantRule);

		populateRequestURL(request,ssoServiceURL);
		when(request.getMethod()).thenReturn("POST");		
		when(request.getParameter("SAMLRequest")).thenReturn(SAML_REQUEST_POST_PARAM);
		when(request.getParameter("Signature")).thenReturn(SIGNATURE_POST_PARAM);
		when(request.getParameter("SigAlg")).thenReturn(SIG_ALG_POST_PARAM);
		when(request.getParameter("KeyInfo")).thenReturn(KEY_INFO_POST_PARAM);
		when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
		when(request.getSession()).thenReturn(session);
		when(session.getCreationTime()).thenReturn(authnInstant.getMillis());
		
		singleSignOnService.handleRequest(request, response);
		
		verify(session).setAttribute(anyString(), captor.capture());
		when(context.getAuthentication()).thenReturn(authToken);
		when(request.getAttribute(AuthnRequest.class.getName())).thenReturn(captor.getValue());
		when(details.getRemoteAddress()).thenReturn(clientIP );
		
		//now try it again
		reset(requestDispatcher);
		
		singleSignOnService.handleRequest(request, response);
		
		verify(requestDispatcher, never()).forward(request, response);
	}
}
