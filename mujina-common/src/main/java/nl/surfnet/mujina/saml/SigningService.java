package nl.surfnet.mujina.saml;

import nl.surfnet.mujina.model.CommonConfiguration;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SigningService {
  private static final Logger logger = LoggerFactory.getLogger(SigningService.class);
  private final CommonConfiguration idpConfiguration;
  private final CredentialResolver credentialResolver;

  @Autowired public SigningService(CommonConfiguration configuration, CredentialResolver credentialResolver) {
    this.idpConfiguration = configuration;
    this.credentialResolver = credentialResolver;
  }

  public void signXMLObject(final SignableXMLObject signableXMLObject) {

    Signature signature = (Signature) org.opensaml.Configuration.getBuilderFactory().getBuilder(
      Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);

    signature.setSigningCredential(getCredential());
    signature.setSignatureAlgorithm(idpConfiguration.getSignatureAlgorithm());
    signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

    signableXMLObject.setSignature(signature);

    try {
      org.opensaml.Configuration.getMarshallerFactory().getMarshaller(signableXMLObject).marshall(signableXMLObject);
    }
    catch (MarshallingException e) {
      logger.error("Cannot marshal signed object: ", e);
    }
    try {
      Signer.signObject(signature);
    }
    catch (SignatureException e) {
      logger.error("SignatureException when signing object: ", e);
    }
  }

  public Credential getCredential() {
    CriteriaSet criteriaSet = new CriteriaSet();
    criteriaSet.add(new EntityIDCriteria(idpConfiguration.getEntityID()));
    criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
    try {
      return credentialResolver.resolveSingle(criteriaSet);
    }
    catch (org.opensaml.xml.security.SecurityException e) {
      logger.error("Unable to resolve EntityID while signing", e);
      return null;
    }
  }

}
