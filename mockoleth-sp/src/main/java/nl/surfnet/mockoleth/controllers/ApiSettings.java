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
package nl.surfnet.mockoleth.controllers;

import org.scribe.model.Token;

/**
 * Backing form object
 * 
 */
public class ApiSettings {
  private String version = "1.0a";
  private String requestTokenEndPoint;
  private String accessTokenEndPoint;
  private String accessTokenEndPoint2;
  private boolean twoLegged;
  private boolean implicitGrant;

  private String oauthKey;
  private String oauthSecret;
  private String authorizationURL;
  private String authorizationURL2;
  private String step;
  private Token requestToken;

  private String requestURL;
  private int count;
  private int startIndex;
  private String sortBy;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public Token getRequestToken() {
    return requestToken;
  }

  public void setRequestToken(Token requestToken) {
    this.requestToken = requestToken;
  }

  public String getStep() {
    return step;
  }

  public void setStep(String step) {
    this.step = step;
  }

  public String getAuthorizationURL() {
    return authorizationURL;
  }

  public void setAuthorizationURL(String authorizationURL) {
    this.authorizationURL = authorizationURL;
  }

  public String getOauthKey() {
    return oauthKey;
  }

  public void setOauthKey(String oauthKey) {
    this.oauthKey = oauthKey;
  }

  public String getOauthSecret() {
    return oauthSecret;
  }

  public void setOauthSecret(String oauthSecret) {
    this.oauthSecret = oauthSecret;
  }

  public boolean isTwoLegged() {
    return twoLegged;
  }

  public void setTwoLegged(boolean twoLegged) {
    this.twoLegged = twoLegged;
  }

  public String getRequestTokenEndPoint() {
    return requestTokenEndPoint;
  }

  public void setRequestTokenEndPoint(String requestTokenEndPoint) {
    this.requestTokenEndPoint = requestTokenEndPoint;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getAccessTokenEndPoint() {
    return accessTokenEndPoint;
  }

  public void setAccessTokenEndPoint(String accessTokenEndPoint) {
    this.accessTokenEndPoint = accessTokenEndPoint;
  }

  public String getAccessTokenEndPoint2() {
    return accessTokenEndPoint2;
  }

  public void setAccessTokenEndPoint2(String accessTokenEndPoint2) {
    this.accessTokenEndPoint2 = accessTokenEndPoint2;
  }

  public boolean isImplicitGrant() {
    return implicitGrant;
  }

  public void setImplicitGrant(boolean implicitGrant) {
    this.implicitGrant = implicitGrant;
  }

  public String getAuthorizationURL2() {
    return authorizationURL2;
  }

  public void setAuthorizationURL2(String authorizationURL2) {
    this.authorizationURL2 = authorizationURL2;
  }

  public String getRequestURL() {
    return requestURL;
  }

  public void setRequestURL(String requestURL) {
    this.requestURL = requestURL;
  }
}
