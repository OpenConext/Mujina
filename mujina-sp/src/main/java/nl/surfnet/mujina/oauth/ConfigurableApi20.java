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

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * @author oharsta
 *
 */
public class ConfigurableApi20 extends DefaultApi20 {

  private String accessTokenEndPoint;
  private String authorizationUrl;
  private boolean implicitGrant;
  
  public ConfigurableApi20(String accessTokenEndPoint, String authorizationUrl, boolean implicitGrant) {
    super();
    this.accessTokenEndPoint = accessTokenEndPoint;
    this.authorizationUrl = authorizationUrl;
    this.implicitGrant = implicitGrant;
  }
  
  @Override
  public OAuthService createService(OAuthConfig config) {
    return new ConfigurableOAuth20ServiceImpl(this, config);
  }


  /* (non-Javadoc)
   * @see org.scribe.builder.api.DefaultApi20#getAccessTokenEndpoint()
   */
  @Override
  public String getAccessTokenEndpoint() {
    return accessTokenEndPoint + "?grant_type=authorization_code";
  }

  /* (non-Javadoc)
   * @see org.scribe.builder.api.DefaultApi20#getAuthorizationUrl(org.scribe.model.OAuthConfig)
   */
  @Override
  public String getAuthorizationUrl(OAuthConfig config) {
    String type = (implicitGrant ? "token" : "code");
    StringBuilder url = new StringBuilder(String.format(authorizationUrl+ "?response_type=%s&client_id=%s", type ,config.getApiKey()));
    if (config.hasScope()) {
      url.append("&scope=").append(config.getScope());
    }
    url.append("&redirect_uri=").append(config.getCallback());
    return url.toString();
  }

  public Verb getAccessTokenVerb() {
    return Verb.POST;
  }

  @Override
  public AccessTokenExtractor getAccessTokenExtractor() {
    // OpenConext (Spring Security OAuth2) sends JSON.
    return new JsonTokenExtractor();
  }

}
