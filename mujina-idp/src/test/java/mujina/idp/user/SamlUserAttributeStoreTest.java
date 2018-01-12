package mujina.idp.user;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.*;

public class SamlUserAttributeStoreTest {

  private SamlUserAttributeStore sut;

  @Test
  public void shouldGetAttributesByUsername() {
    // given
    List<SamlUser> users = Arrays.asList(
      createSamlUser("user 1", 2),
      createSamlUser("user 2", 1));
    sut = new SamlUserAttributeStore(users);

    // when
    Map<String, List<String>> user1Attributes = sut.getAttributesByUsername("user 1");
    Map<String, List<String>> user2Attributes = sut.getAttributesByUsername("user 2");

    // then
    assertThat(user1Attributes.size(), is(2));
    assertThat(user1Attributes.containsKey("attribute 1"), is(true));
    assertThat(user1Attributes.get("attribute 1").contains("att 1 : val 1"), is(true));
    assertThat(user2Attributes.size(), is(1));
  }

  @Test
  public void shouldGetEmptyMapForUnknownUser() {
    // given
    List<SamlUser> samlUsers = Collections.singletonList(createSamlUser("user 1", 2));
    sut = new SamlUserAttributeStore(samlUsers);

    // when
    Map<String, List<String>> user1Attributes = sut.getAttributesByUsername("user x");

    // then
    assertThat(user1Attributes, is(Collections.emptyMap()));
  }

  private SamlUser createSamlUser(String username, int attributeCount) {
    Map<String, List<String>> samlAttributes = getAttributeMap(attributeCount);
    return new SamlUser(username, samlAttributes);
  }

  private Map<String, List<String>> getAttributeMap(int attributeCount) {
    Map<String, List<String>> samlAttributes = new HashMap<>();
    samlAttributes.put("attribute 1", Arrays.asList("att 1 : val 1", "att 1 : val 2"));

    if (attributeCount > 1) {
      samlAttributes.put("attribute 2", Arrays.asList("att 2 : val 1", "att 2 : val 2"));
    }

    return samlAttributes;
  }
}
