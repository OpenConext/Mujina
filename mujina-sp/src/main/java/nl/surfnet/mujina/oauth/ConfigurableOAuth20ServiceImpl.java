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
package nl.surfnet.mujina.oauth;

import org.apache.commons.codec.binary.Base64;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;

/**
 * Because of poor extensibility we copied the {@link OAuth20ServiceImpl} from
 * srcibe to display the in-between-steps.
 * 
 */
public class ConfigurableOAuth20ServiceImpl implements OAuthService {

  private static final String VERSION = "2.0";

  private final DefaultApi20 api;
  private final OAuthConfig config;

  /**
   * Default constructor
   * 
   * @param api
   *          OAuth2.0 api information
   * @param config
   *          OAuth 2.0 configuration param object
   */
  public ConfigurableOAuth20ServiceImpl(DefaultApi20 api, OAuthConfig config) {
    this.api = api;
    this.config = config;
  }

  public OAuthRequest getOAuthRequest(Verifier verifier, boolean useQueryString) {
    OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    if (useQueryString) {
      request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
      request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
      request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
      request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
      if (config.hasScope())
        request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
    } else {
      request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
      request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
      request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
      request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
      if (config.hasScope())
        request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());

    }
    return request;
  }

  public OAuthRequest getOAuthRequestConformSpec(Verifier verifier) {
    OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    
    String encodeBase64String = Base64.encodeBase64String((config.getApiKey() + ":" + config.getApiSecret()).getBytes());
    encodeBase64String = encodeBase64String.replaceAll("\n", "").replaceAll("\r", "");
    request.addHeader("Authorization",
        "Basic " + encodeBase64String);
    request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
    request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
    request.addBodyParameter("grant_type", "authorization_code");
    if (config.hasScope()) {
      request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());
    }
    return request;
  }

  public Response getOauthResponse(OAuthRequest request) {
    Response response = request.send();
    return response;
  }

  public Token getAccessToken(Response response) {
    return api.getAccessTokenExtractor().extract(response.getBody());
  }

  /**
   * {@inheritDoc}
   */
  public Token getAccessToken(Token requestToken, Verifier verifier) {

    Response response = getOauthResponse(getOAuthRequest(verifier, true));
    return api.getAccessTokenExtractor().extract(response.getBody());
  }

  /**
   * {@inheritDoc}
   */
  public Token getRequestToken() {
    throw new UnsupportedOperationException(
        "Unsupported operation, please use 'getAuthorizationUrl' and redirect your users there");
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
  public void signRequest(Token accessToken, OAuthRequest request) {
    request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getToken());
  }

  /**
   * {@inheritDoc}
   */
  public String getAuthorizationUrl(Token requestToken) {
    return api.getAuthorizationUrl(config);
  }

  /**
   * @param accessToken
   * @param oAuthRequest
   */
  public void signRequestAsHeader(Token accessToken, OAuthRequest request) {
    request.addHeader("Authorization", "Bearer " + accessToken.getToken());
  }

}
