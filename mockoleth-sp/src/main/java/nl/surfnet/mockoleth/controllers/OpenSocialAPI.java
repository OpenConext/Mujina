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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.mockoleth.oauth.ConfigurableApi10a;
import nl.surfnet.mockoleth.oauth.ConfigurableOAuth10aServiceImpl;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * 
 * http://stackoverflow.com/questions/8573272/oauth-problems-with-scribe
 * http://tutorials.jenkov.com/oauth2/authorization-code-request-response.html
 */
@Controller
public class OpenSocialAPI {

  private final ObjectMapper objectMapper = new ObjectMapper().enable(
      DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY).setSerializationInclusion(
      JsonSerialize.Inclusion.NON_NULL);

  // @Value("${OAUTH_CALLBACK_URL}")
  private String oauthCallbackUrl;

  public OpenSocialAPI() {
    super();
    // @TODO find out why this does not get normally resolved by @Value. This is
    // a hack.
    Properties properties = new Properties();
    try {
      properties.load(new ClassPathResource("mockoleth-sp.properties").getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    oauthCallbackUrl = properties.getProperty("OAUTH_CALLBACK_URL");
  }

  @RequestMapping(value = { "/social-queries.shtml" }, method = RequestMethod.GET)
  public String socialQueries(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    setupModelMap(new ApiSettings(), "step1", request, modelMap, null);
    return "social-queries";
  }

  @RequestMapping(value = "/api.shtml", method = RequestMethod.POST, params = "step1")
  public String step1(ModelMap modelMap, @ModelAttribute("settings")
  ApiSettings settings, HttpServletRequest request, HttpServletResponse response) throws IOException {
    ConfigurableOAuth10aServiceImpl service = getService10(settings);
    OAuthRequest oAuthRequest = service.getOAuthRequest();
    Response oauthResponse = service.getOauthResponse(oAuthRequest);
    Token requestToken = service.getRequestToken(oauthResponse);
    String authorizationUrl = service.getAuthorizationUrl(requestToken);
    settings.setRequestToken(requestToken);
    modelMap.addAttribute("requestInfo", oAuthRequestToString(oAuthRequest));
    modelMap.addAttribute("responseInfo", oAuthResponseToString(oauthResponse));
    modelMap.addAttribute("authorizationUrlAfter", authorizationUrl);
    setupModelMap(settings, "step2", request, modelMap, service);
    return "social-queries";
  }

  private static String oAuthRequestToString(OAuthRequest request) {
    return String
        .format("@OAuthRequest(%s, %s, %s)", request.getVerb(), request.getUrl(), request.getOauthParameters());
  }

  private static String oAuthResponseToString(Response response) {
    return String.format("@OAuthResponse(%s, %s)", response.getHeaders(), response.getBody());
  }

  @RequestMapping(value = "/api.shtml", method = RequestMethod.POST, params = "step2")
  public void step2(ModelMap modelMap, @ModelAttribute("settings")
  ApiSettings settings, HttpServletRequest request, HttpServletResponse response) throws IOException {
    ConfigurableOAuth10aServiceImpl service = (ConfigurableOAuth10aServiceImpl) request.getSession().getAttribute(
        "service");
    ApiSettings settingsFromSession = (ApiSettings) request.getSession().getAttribute("settings");
    Token requestToken = (Token) settingsFromSession.getRequestToken();
    String authorizationUrl = service.getAuthorizationUrl(requestToken);
    response.sendRedirect(authorizationUrl);
  }

  @RequestMapping(value = "/api.shtml", method = RequestMethod.POST, params = "step3")
  public String step3(ModelMap modelMap, @ModelAttribute("settings")
  ApiSettings settings, HttpServletRequest request, HttpServletResponse response) throws IOException {
    ConfigurableOAuth10aServiceImpl service = (ConfigurableOAuth10aServiceImpl) request.getSession().getAttribute(
        "service");// getService10(settings);
    Token accessToken = (Token) request.getSession().getAttribute("accessToken");
    StringBuffer requestURL = new StringBuffer(settings.getRequestURL() + "?");
    if (settings.getCount() != 0) {
      requestURL.append("count=" + settings.getCount() + "&");
    }
    if (settings.getStartIndex() != 0) {
      requestURL.append("startIndex=" + settings.getStartIndex() + "&");
    }
    if (StringUtils.hasText(settings.getSortBy())) {
      requestURL.append("sortBy=" + settings.getSortBy() + "&");
    }
    OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, requestURL.toString());
    service.signRequest(accessToken, oAuthRequest);
    Response oAuthResponse = oAuthRequest.send();
    modelMap.addAttribute("requestInfo", oAuthRequestToString(oAuthRequest));
    modelMap.addAttribute("responseInfo", oAuthResponseToString(oAuthResponse));
    modelMap.addAttribute("accessToken", accessToken);
    setupModelMap(settings, "step3", request, modelMap, service);
    return "social-queries";

  }

  @RequestMapping(value = "/api.shtml", method = RequestMethod.POST, params = "reset")
  public String reset(ModelMap modelMap, @ModelAttribute("settings")
  ApiSettings settings, HttpServletRequest request, HttpServletResponse response) throws IOException {
    return socialQueries(modelMap, request, response);
  }

  @RequestMapping(value = "/oauth-callback.shtml", method = RequestMethod.GET)
  public String oauthCallBack(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    String oAuthVerifier = request.getParameter("oauth_verifier");
    Verifier verifier = new Verifier(oAuthVerifier);
    ConfigurableOAuth10aServiceImpl service = (ConfigurableOAuth10aServiceImpl) request.getSession().getAttribute(
        "service");
    ApiSettings settings = (ApiSettings) request.getSession().getAttribute("settings");
    Token requestToken = settings.getRequestToken();
    OAuthRequest oAuthRequest = service.getAccessTokenRequest(requestToken, verifier);
    Response oauthResponse = service.getAccessTokenResponse(oAuthRequest);
    Token accessToken = service.getAccessTokenFromResponse(oauthResponse);
    request.getSession().setAttribute("accessToken", accessToken);
    modelMap.addAttribute("requestInfo", oAuthRequestToString(oAuthRequest));
    modelMap.addAttribute("responseInfo", oAuthResponseToString(oauthResponse));
    modelMap.addAttribute("accessToken", accessToken);
    setupModelMap(settings, "step3", request, modelMap, service);
    return "social-queries";
  }

  @ModelAttribute("versions")
  public Collection<String> populateOAuthVersions() {
    return Arrays.asList(new String[] { "1.0a", "2.0" });
  }

  private void setupModelMap(ApiSettings settings, String step, HttpServletRequest request, ModelMap modelMap,
      ConfigurableOAuth10aServiceImpl service) {
    settings.setStep(step);
    modelMap.addAttribute("settings", settings);
    request.getSession().setAttribute("settings", settings);
    request.getSession().setAttribute("service", service);
  }

  private ConfigurableOAuth10aServiceImpl getService10(ApiSettings settings) {
    ConfigurableOAuth10aServiceImpl service = (ConfigurableOAuth10aServiceImpl) new ServiceBuilder()
        .provider(
            new ConfigurableApi10a(settings.getRequestTokenEndPoint(), settings.getAuthorizationURL(), settings
                .getAccessTokenEndPoint())).apiKey(settings.getOauthKey()).apiSecret(settings.getOauthSecret())
        .callback(oauthCallbackUrl).debug().build();
    return service;
  }

}
