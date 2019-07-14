package mujina.api;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

  private final ErrorAttributes errorAttributes;

  public ErrorController(ErrorAttributes errorAttributes) {
    Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
    this.errorAttributes = errorAttributes;
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }

  @RequestMapping
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest aRequest) {
    ServletWebRequest webRequest = new ServletWebRequest(aRequest);
    Map<String, Object> result = this.errorAttributes.getErrorAttributes(webRequest, false);

    HttpStatus statusCode = INTERNAL_SERVER_ERROR;
    Object status = result.get("status");
    if (status != null && status instanceof Integer) {
      statusCode = HttpStatus.valueOf(((Integer) status).intValue());
    }
    return new ResponseEntity<>(result, statusCode);

  }

}
