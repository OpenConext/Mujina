package mujina.api.sp;

import mujina.api.SharedController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/sp/ api", consumes = "application/json")
@ConditionalOnProperty(prefix = "sp.api", name = "enabled")
public class SpController extends SharedController {

  @Autowired
  public SpController(final SpConfiguration configuration) {
    super(configuration);
  }

  @PutMapping(value = {"/ssoServiceURL"})
  public void setSsoServiceURL(@RequestBody String ssoServiceURL) {
    LOG.debug("Request to set ssoServiceURL to {}", ssoServiceURL);
    configuration().setIdpSSOServiceURL(ssoServiceURL);
  }

  @PutMapping("/protocolBinding")
  public void setProtocolBinding(@RequestBody String protocolBinding) {
    LOG.debug("Request to set protocolBinding to {}", protocolBinding);
    configuration().setProtocolBinding(protocolBinding);
  }

  @PutMapping("/assertionConsumerServiceURL")
  public void setAssertionConsumerServiceURL(@RequestBody String assertionConsumerServiceURL) {
    LOG.debug("Request to set assertionConsumerServiceURL to {}", assertionConsumerServiceURL);
    configuration().setAssertionConsumerServiceURL(assertionConsumerServiceURL);
  }

  private SpConfiguration configuration() {
    return SpConfiguration.class.cast(super.configuration);
  }

}
