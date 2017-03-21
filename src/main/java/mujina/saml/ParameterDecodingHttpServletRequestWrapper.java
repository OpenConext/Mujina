package mujina.saml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.net.URLDecoder;

public class ParameterDecodingHttpServletRequestWrapper extends HttpServletRequestWrapper {

  public ParameterDecodingHttpServletRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  @Override
  public String getParameter(String name) {
    String parameter = super.getParameter(name);
    return parameter != null ? decode(parameter) : null;
  }

  @SuppressWarnings("deprecation")
  private String decode(String parameter) {
    return URLDecoder.decode(parameter);
  }


}
