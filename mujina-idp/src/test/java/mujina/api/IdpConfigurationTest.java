package mujina.api;

import mujina.AbstractIntegrationTest;
import mujina.idp.user.SamlUserStore;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.JKSKeyManager;

import static org.mockito.Mockito.*;

public class IdpConfigurationTest extends AbstractIntegrationTest {

  private IdpConfiguration sut;

  private SamlUserStore samlUserStore;

  @Autowired
  private JKSKeyManager jksKeyManager;

  @Value("${idp.entity_id}")
  private String idpEntityId;
  @Value("${idp.private_key}")
  private String idpPrivateKey;
  @Value("${idp.certificate}")
  private String idpCertificate;
  @Value("${idp.auth_method}")
  private String authMethod;

  @Before
  public void setup() {
    samlUserStore = mock(SamlUserStore.class);
  }

  @Test
  public void shouldNotResetUserAttributesOnConstruction() {
    // given
    // when
    sut = new IdpConfiguration(jksKeyManager, idpEntityId, idpPrivateKey, idpCertificate, authMethod, samlUserStore);

    // then
    verify(samlUserStore, never()).resetDynamicUsers();
    verify(samlUserStore, never()).resetSeedDataUserAttributes();
  }

  @Test
  public void shouldResetUserAttributesOnResetAfterConstruction() {
    // given
    sut = new IdpConfiguration(jksKeyManager, idpEntityId, idpPrivateKey, idpCertificate, authMethod, samlUserStore);

    // when
    sut.reset();

    // then
    verify(samlUserStore, times(1)).resetDynamicUsers();
    verify(samlUserStore, times(1)).resetSeedDataUserAttributes();
  }
}
