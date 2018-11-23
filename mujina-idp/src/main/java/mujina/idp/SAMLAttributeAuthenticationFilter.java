package mujina.idp;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.stream.Collectors;

public class SAMLAttributeAuthenticationFilter extends UsernamePasswordAuthenticationFilter {


  @Override
  protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
    Map<String, String[]> parameterMap = request.getParameterMap().entrySet().stream()
      .filter(e -> !getPasswordParameter().equals(e.getKey()) && !getUsernameParameter().equals(e.getKey()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    authRequest.setDetails(parameterMap);
  }
}
