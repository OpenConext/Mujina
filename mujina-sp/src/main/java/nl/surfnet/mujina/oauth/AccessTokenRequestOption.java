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

/**
 * The different ways to obtain an AccessToken (see
 * http://tools.ietf.org/html/draft-ietf-oauth-v2-26#section-4.1.3)
 * 
 */
public enum AccessTokenRequestOption {

  AUTHENTICATION_HEADER("Authentication Header"), QUERY_STRING_PARAMETERS("Query String Parameters"), ENTITY_BODY_PARAMETERS(
      "Entity Body Parameters");

  private String option;

  private AccessTokenRequestOption(String option) {
    this.option = option;
  }

  public String getOption() {
    return option;
  }

  public static AccessTokenRequestOption valueOfOption(String optionValue) {
    AccessTokenRequestOption[] values = AccessTokenRequestOption.values();
    for (AccessTokenRequestOption option : values) {
      if (option.getOption().equalsIgnoreCase(optionValue)) {
        return option;
      }
    }
    throw new RuntimeException(
        String.format("Can't convert %s to an instance of AccessTokenRequestOption", optionValue));
  }
}
