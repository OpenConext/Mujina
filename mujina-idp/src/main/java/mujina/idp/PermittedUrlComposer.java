package mujina.idp;

import java.util.stream.Stream;

class PermittedUrlComposer {

  private static final String[] baseUrls = new String[]{"/", "/metadata", "/favicon.ico", "/laa_logo.png", "/*.css"};
  private static final String[] apiUrl = new String[]{"/api/**"};

  private final boolean apiEnabled;

  PermittedUrlComposer(boolean apiEnabled) {
    this.apiEnabled = apiEnabled;
  }

  String[] getPermittedUrls() {
    if (apiEnabled) {
      return Stream.of(baseUrls, apiUrl).flatMap(Stream::of).toArray(String[]::new);
    } else {
      return baseUrls;
    }
  }

  String[] getDeniedUrls() {
    if (!apiEnabled) {
      return apiUrl;
    }
    else {
      return new String[]{};
    }
  }
}
