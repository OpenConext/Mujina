/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package nl.surfnet.mockoleth.oauth;

import java.util.Map;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth10aServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.MapUtils;

/**
 * Because of poor extensibility we copied the {@link OAuth10aServiceImpl} from
 * srcibe to display the in-between-steps and to add info during the process to
 * the {@link DefaultApi10a}.
 * 
 */
public class ConfigurableOAuth10aServiceImpl implements OAuthService {
  private static final String VERSION = "1.0";

  private OAuthConfig config;
  private DefaultApi10a api;

  /**
   * Default constructor
   * 
   * @param api
   *          OAuth1.0a api information
   * @param config
   *          OAuth 1.0a configuration param object
   */
  public ConfigurableOAuth10aServiceImpl(DefaultApi10a api, OAuthConfig config) {
    this.api = api;
    this.config = config;
  }

  public DefaultApi10a getApi() {
    return api;
  }

  public OAuthRequest getOAuthRequest() {
    config.log("obtaining request token from " + api.getRequestTokenEndpoint());
    OAuthRequest request = new OAuthRequest(api.getRequestTokenVerb(), api.getRequestTokenEndpoint());

    config.log("setting oauth_callback to " + config.getCallback());
    request.addOAuthParameter(OAuthConstants.CALLBACK, config.getCallback());
    addOAuthParams(request, OAuthConstants.EMPTY_TOKEN);
    appendSignature(request);
    return request;
  }

  public Response getOauthResponse(OAuthRequest request) {
    config.log("sending request...");
    return request.send();
  }

  public Token getRequestToken(Response response) {
    String body = response.getBody();

    config.log("response status code: " + response.getCode());
    config.log("response body: " + body);
    return api.getRequestTokenExtractor().extract(body);
  }

  /**
   * {@inheritDoc}
   */
  public Token getRequestToken() {
    OAuthRequest request = getOAuthRequest();
    Response response = getOauthResponse(request);
    return getRequestToken(response);
  }

  private void addOAuthParams(OAuthRequest request, Token token) {
    request.addOAuthParameter(OAuthConstants.TIMESTAMP, api.getTimestampService().getTimestampInSeconds());
    request.addOAuthParameter(OAuthConstants.NONCE, api.getTimestampService().getNonce());
    request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, config.getApiKey());
    request.addOAuthParameter(OAuthConstants.SIGN_METHOD, api.getSignatureService().getSignatureMethod());
    request.addOAuthParameter(OAuthConstants.VERSION, getVersion());
    if (config.hasScope())
      request.addOAuthParameter(OAuthConstants.SCOPE, config.getScope());
    request.addOAuthParameter(OAuthConstants.SIGNATURE, getSignature(request, token));

    config.log("appended additional OAuth parameters: " + MapUtils.toString(request.getOauthParameters()));
  }

  /**
   * {@inheritDoc}
   */
  public Token getAccessToken(Token requestToken, Verifier verifier) {
    config.log("obtaining access token from " + api.getAccessTokenEndpoint());
    OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    request.addOAuthParameter(OAuthConstants.TOKEN, requestToken.getToken());
    request.addOAuthParameter(OAuthConstants.VERIFIER, verifier.getValue());

    config.log("setting token to: " + requestToken + " and verifier to: " + verifier);
    addOAuthParams(request, requestToken);
    appendSignature(request);
    Response response = request.send();
    return api.getAccessTokenExtractor().extract(response.getBody());
  }

  public OAuthRequest getAccessTokenRequest(Token requestToken, Verifier verifier) {
    OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    request.addOAuthParameter(OAuthConstants.TOKEN, requestToken.getToken());
    request.addOAuthParameter(OAuthConstants.VERIFIER, verifier.getValue());

    config.log("setting token to: " + requestToken + " and verifier to: " + verifier);
    addOAuthParams(request, requestToken);
    appendSignature(request);
    return request;

  }

  public Response getAccessTokenResponse(OAuthRequest request) {
    return request.send();
  }

  public Token getAccessTokenFromResponse(Response response) {
    return api.getAccessTokenExtractor().extract(response.getBody());
  }
  
  
  /**
   * {@inheritDoc}
   */
  public void signRequest(Token token, OAuthRequest request) {
    config.log("signing request: " + request.getCompleteUrl());
    request.addOAuthParameter(OAuthConstants.TOKEN, token.getToken());

    config.log("setting token to: " + token);
    addOAuthParams(request, token);
    appendSignature(request);
  }

  /**
   * {@inheritDoc}
   */
  public String getVersion() {
    return VERSION;
  }

  /**
   * {@inheritDoc}
   */
  public String getAuthorizationUrl(Token requestToken) {
    return api.getAuthorizationUrl(requestToken);
  }

  private String getSignature(OAuthRequest request, Token token) {
    config.log("generating signature...");
    String baseString = api.getBaseStringExtractor().extract(request);
    String signature = api.getSignatureService().getSignature(baseString, config.getApiSecret(), token.getSecret());

    config.log("base string is: " + baseString);
    config.log("signature is: " + signature);
    return signature;
  }

  private void appendSignature(OAuthRequest request) {
    switch (config.getSignatureType()) {
    case Header:
      config.log("using Http Header signature");

      String oauthHeader = api.getHeaderExtractor().extract(request);
      request.addHeader(OAuthConstants.HEADER, oauthHeader);
      break;
    case QueryString:
      config.log("using Querystring signature");

      for (Map.Entry<String, String> entry : request.getOauthParameters().entrySet()) {
        request.addQuerystringParameter(entry.getKey(), entry.getValue());
      }
      break;
    }
  }

}
