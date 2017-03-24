package mujina.sp;

import org.springframework.security.saml.storage.SAMLMessageStorage;
import org.springframework.security.saml.storage.SAMLMessageStorageFactory;

import javax.servlet.http.HttpServletRequest;

public class InMemorySAMLMessageStorageFactory implements SAMLMessageStorageFactory {

  private InMemorySAMLMessageStorage inMemorySAMLMessageStorage = new InMemorySAMLMessageStorage();

  @Override
  public SAMLMessageStorage getMessageStorage(HttpServletRequest request) {
    return inMemorySAMLMessageStorage;
  }
}
