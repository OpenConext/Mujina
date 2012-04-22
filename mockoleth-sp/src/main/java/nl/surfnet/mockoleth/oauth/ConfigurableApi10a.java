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

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

/**
 * {@link DefaultApi10a} that is run-time configurable
 *
 */
public class ConfigurableApi10a extends DefaultApi10a {

  private String requestTokenEndpoint;
  private String authorizationUrl;
  private String accessTokenEndpoint;

  public ConfigurableApi10a(String requestTokenEndpoint, String authorizationUrl, String accessTokenEndpoint) {
    this.requestTokenEndpoint = requestTokenEndpoint;
    this.authorizationUrl = authorizationUrl;
    this.accessTokenEndpoint = accessTokenEndpoint;
  }

  public String getAuthorizationUrl() {
    return authorizationUrl;
  }

  @Override
  public OAuthService createService(OAuthConfig config) {
    return new ConfigurableOAuth10aServiceImpl(this, config);
  }

  public void setAuthorizationUrl(String authorizationUrl) {
    this.authorizationUrl = authorizationUrl;
  }

  public void setRequestTokenEndpoint(String requestTokenEndpoint) {
    this.requestTokenEndpoint = requestTokenEndpoint;
  }

  public void setAccessTokenEndpoint(String accessTokenEndpoint) {
    this.accessTokenEndpoint = accessTokenEndpoint;
  }

  /* (non-Javadoc)
   * @see org.scribe.builder.api.DefaultApi10a#getRequestTokenEndpoint()
   */
  @Override
  public String getRequestTokenEndpoint() {
    return requestTokenEndpoint;
  }


  /* (non-Javadoc)
   * @see org.scribe.builder.api.DefaultApi10a#getAuthorizationUrl(org.scribe.model.Token)
   */
  @Override
  public String getAuthorizationUrl(Token requestToken) {
    return authorizationUrl + "?oauth_token="+requestToken.getToken();
  }

  /* (non-Javadoc)
   * @see org.scribe.builder.api.DefaultApi10a#getAccessTokenEndpoint()
   */
  @Override
  public String getAccessTokenEndpoint() {
    return accessTokenEndpoint;
  }

}
