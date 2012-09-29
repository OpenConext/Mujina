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

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

/**
 * Will accept all certificates
 * 
 */
public class OAuthRequestIgnoreCertificate extends OAuthRequest {

  /**
   * @param verb
   * @param url
   */
  public OAuthRequestIgnoreCertificate(Verb verb, String url) {
    super(verb, url);
    try {
      createUnsafeConnection();
    } catch (Exception e) {
      throw new OAuthException("Problems while creating connection.", e);
    }
  }

  /*
   * Incredible dirty hack to ensire we have a Connection that does not check
   * certificates. The extensibility of OAuthRequest is bad and therefore the
   * reflection 'trick'
   */
  private void createUnsafeConnection() throws Exception {
    System.setProperty("http.keepAlive", "true");
    String completeUrl = getCompleteUrl();
    Field connField = getClass().getSuperclass().getSuperclass().getDeclaredField("connection");
    connField.setAccessible(true);
    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }
    } };
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    URLConnection openConnection = new URL(completeUrl).openConnection();
    connField.set(this, openConnection);
  }

}
