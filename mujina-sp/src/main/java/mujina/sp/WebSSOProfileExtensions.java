package mujina.sp;

import mujina.saml.SAMLBuilder;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.common.impl.ExtensionsBuilder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import javax.xml.namespace.QName;

public class WebSSOProfileExtensions extends WebSSOProfileImpl {

  @Override
  protected AuthnRequest getAuthnRequest(SAMLMessageContext context, WebSSOProfileOptions options, AssertionConsumerService assertionConsumer, SingleSignOnService bindingService) throws SAMLException, MetadataProviderException {
    AuthnRequest authnRequest = super.getAuthnRequest(context, options, assertionConsumer, bindingService);
    authnRequest.setExtensions(buildExtensions());
    return authnRequest;
  }

  protected Extensions buildExtensions() {
    Extensions extensions = new ExtensionsBuilder().buildObject();
    XSAny policyClass1 = new XSAnyBuilder().buildObject("https://openconext/schema/ext", "Policy", "policy");
    policyClass1.setTextContent("urn:type:policy:consent");
    policyClass1.getUnknownAttributes().put(new QName("policyAttribute"), "value");
    extensions.getUnknownXMLObjects().add(policyClass1);
    return extensions;
  }
}
