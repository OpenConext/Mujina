package mujina.idp.user;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.*;

public class SamlUserStoreTest {

  private SamlUserStore sut;

  @Test
  public void shouldHaveEmptyUserList() {
    // given
    sut = new SamlUserStore();

    // when
    // then
    assertThat(sut.getSamlUsers(), is(Collections.emptyList()));
  }

  @Test
  public void shouldGetAttributeForDefaultUsers() {
    // given
    sut = new SamlUserStore();

    // when
    Map<String, String> adminAttributes = sut.getAttributesByUsername("admin");
    Map<String, String> userAttributes = sut.getAttributesByUsername("user");

    // then
    assertThat(adminAttributes.size(), is(1));
    assertThat(adminAttributes.containsKey("attribute 1"), is(true));
    assertThat(adminAttributes.get("attribute 1"), is("admin attribute 1"));

    assertThat(userAttributes.size(), is(1));
    assertThat(userAttributes.containsKey("attribute 1"), is(true));
    assertThat(userAttributes.get("attribute 1"), is("user attribute 1"));
  }

  @Test
  public void shouldGetAttributesByUsername() {
    // given
    List<SamlUser> users = Arrays.asList(
      createSamlUser("user 1", 2),
      createSamlUser("user 2", 1));
    sut = new SamlUserStore(users);

    // when
    Map<String, String> user1Attributes = sut.getAttributesByUsername("user 1");
    Map<String, String> user2Attributes = sut.getAttributesByUsername("user 2");

    // then
    assertThat(user1Attributes.size(), is(2));
    assertThat(user1Attributes.containsKey("attribute 1"), is(true));
    assertThat(user1Attributes.get("attribute 1").contains("att 1:val 1,val 2"), is(true));
    assertThat(user1Attributes.containsKey("attribute 2"), is(true));
    assertThat(user1Attributes.get("attribute 2").contains("att 2:val 1,val 2"), is(true));

    assertThat(user2Attributes.size(), is(1));
    assertThat(user2Attributes.containsKey("attribute 1"), is(true));
    assertThat(user2Attributes.get("attribute 1").contains("att 1:val 1,val 2"), is(true));
  }

  @Test
  public void shouldGetEmptyMapForUnknownUser() {
    // given
    List<SamlUser> samlUsers = Collections.singletonList(createSamlUser("user 1", 2));
    sut = new SamlUserStore(samlUsers);

    // when
    Map<String, String> user1Attributes = sut.getAttributesByUsername("user x");

    // then
    assertThat(user1Attributes, is(Collections.emptyMap()));
  }

  private static SamlUser createSamlUser(String username, int attributeCount) {
    Map<String, String> samlAttributes = getAttributeMap(attributeCount);
    return new SamlUser(username, "password", samlAttributes);
  }

  private static Map<String, String> getAttributeMap(int attributeCount) {
    Map<String, String> samlAttributes = new HashMap<>();
    samlAttributes.put("attribute 1", "att 1:val 1,val 2");

    if (attributeCount > 1) {
      samlAttributes.put("attribute 2", "att 2:val 1,val 2");
    }

    return samlAttributes;
  }
}
