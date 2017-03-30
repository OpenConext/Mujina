package mujina.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

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
    RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
    Map<String, Object> result = this.errorAttributes.getErrorAttributes(requestAttributes, false);

    HttpStatus statusCode = INTERNAL_SERVER_ERROR;
    Object status = result.get("status");
    if (status != null && status instanceof Integer) {
      statusCode = HttpStatus.valueOf(((Integer) status).intValue());
    }
    return new ResponseEntity<>(result, statusCode);

  }

}
