package mujina.saml;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "nameID")
public class SAMLPrincipal implements Principal {

  private String serviceProviderEntityID;
  private String requestID;
  private String assertionConsumerServiceURL;
  private String relayState;

  private final List<SAMLAttribute> attributes = new ArrayList<>();

  private String nameID;
  private String nameIDType;

  public SAMLPrincipal(String nameID, String nameIDType, List<SAMLAttribute> attributes) {
    this.nameID = nameID;
    this.nameIDType = nameIDType;
    this.attributes.addAll(attributes);
  }

  public SAMLPrincipal(String nameID, String nameIDType, List<SAMLAttribute> attributes, String serviceProviderEntityID, String requestID, String assertionConsumerServiceURL, String relayState) {
    this(nameID, nameIDType, attributes);
    this.serviceProviderEntityID = serviceProviderEntityID;
    this.requestID = requestID;
    this.assertionConsumerServiceURL = assertionConsumerServiceURL;
    this.relayState = relayState;
  }

  @Override
  public String getName() {
    return nameID;
  }
}
