package mujina.sp;

import org.opensaml.xml.XMLObject;
import org.springframework.security.saml.storage.SAMLMessageStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySAMLMessageStorage implements SAMLMessageStorage {

  private Map<String, XMLObject> storage = new ConcurrentHashMap<>();

  @Override
  public void storeMessage(String messageId, XMLObject message) {
    storage.put(messageId, message);
  }

  @Override
  public XMLObject retrieveMessage(String messageID) {
    return storage.get(messageID);
  }
}
