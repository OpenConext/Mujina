package mujina.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class SharedController {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  protected SharedConfiguration configuration;

  public SharedController(SharedConfiguration configuration) {
    this.configuration = configuration;
  }

  @PostMapping("/reset")
  public void reset() {
    LOG.debug("Resetting to default configuration");
    configuration.reset();
  }

  @PutMapping("/entityid")
  public void setEntityID(@RequestBody String entityID) {
    LOG.debug("Request to set entityID {}", entityID);
    configuration.setEntityId(entityID);
  }

  @PostMapping("/signing-credential")
  public void setSigningCredential(@RequestBody Credential credential) {
    LOG.debug("Request to set signing credential {}", credential);
    configuration.injectCredential(credential.getCertificate(), credential.getKey());
  }

  @PutMapping("/needs-signing")
  public void setSigningNeeded(@RequestBody boolean needsSigning) {
    LOG.debug("Request to set signing needed {}", needsSigning);
    configuration.setNeedsSigning(needsSigning);
  }

  @GetMapping("/configuration")
  public SharedConfiguration conf() {
    LOG.debug("Request to receive configuration");
    return configuration;
  }
}
