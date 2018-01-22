package mujina.idp.user;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    Map<String, String> adminAttributes = sut.getUserAttributes("admin");
    Map<String, String> userAttributes = sut.getUserAttributes("user");

    // then
    assertThat(adminAttributes.size(), is(1));
    assertThat(adminAttributes.containsKey("attribute 1"), is(true));
    assertThat(adminAttributes.get("attribute 1"), is("admin attribute 1"));

    assertThat(userAttributes.size(), is(1));
    assertThat(userAttributes.containsKey("attribute 1"), is(true));
    assertThat(userAttributes.get("attribute 1"), is("user attribute 1"));
  }

  @Test
  public void shouldAttributesForSeedUsers() {
    // given
    List<SamlUser> users = Arrays.asList(
      createSamlUser("user 1", 2),
      createSamlUser("user 2", 1));
    sut = new SamlUserStore(users);

    // when
    Map<String, String> user1Attributes = sut.getUserAttributes("user 1");
    Map<String, String> user2Attributes = sut.getUserAttributes("user 2");

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
    List<SamlUser> samlUsers = singletonList(createSamlUser("user 1", 2));
    sut = new SamlUserStore(samlUsers);

    // when
    Map<String, String> user1Attributes = sut.getUserAttributes("user x");

    // then
    assertThat(user1Attributes, is(Collections.emptyMap()));
  }

  @Test
  public void shouldSetUserAttributesWithList() {
    // given
    List<SamlUser> samlUsers = singletonList(createSamlUser("user 1", 1));
    sut = new SamlUserStore(samlUsers);

    // when
    sut.setUserAttributes("user 1", "attribute 1", asList("attribute value 1", "attribute value 2"));

    // then
    Map<String, String> attributes = sut.getUserAttributes("user 1");
    assertThat(attributes.size(), is(1));
    assertThat(attributes.containsKey("attribute 1"), is(true));
    assertThat(attributes.get("attribute 1"), is("attribute value 1,attribute value 2"));
  }

  @Test
  public void shouldSetUserAttributesWithMap() {
    // given
    List<SamlUser> samlUsers = singletonList(createSamlUser("user 1", 1));
    sut = new SamlUserStore(samlUsers);

    // when
    sut.setUserAttributes("user 1", Collections.singletonMap("attribute 1", asList("attribute value 1", "attribute value 2")));

    // then
    Map<String, String> attributes = sut.getUserAttributes("user 1");
    assertThat(attributes.size(), is(1));
    assertThat(attributes.containsKey("attribute 1"), is(true));
    assertThat(attributes.get("attribute 1"), is("attribute value 1,attribute value 2"));
  }

  @Test
  public void shouldRemoveUserAttribute() {
    // given
    List<SamlUser> samlUsers = singletonList(createSamlUser("user 1", 1));
    sut = new SamlUserStore(samlUsers);

    // when
    sut.removeUserAttribute("user 1", "attribute 1");

    // then
    Map<String, String> attributes = sut.getUserAttributes("user 1");
    assertThat(attributes.size(), is(0));
  }

  @Test
  public void shouldAddDynamicUser() {
    // given
    sut = new SamlUserStore();

    // when
    sut.addDynamicUser("dynamic user", "dynamic password");

    // then
    boolean dynamicUserExists = sut.getSamlUsers().stream()
      .anyMatch(samlUser -> samlUser.getUsername().equals("dynamic user") && samlUser.isDynamic());

    assertThat(dynamicUserExists, is(true));
  }

  @Test
  public void shouldNotAddDuplicateDynamicUsers() {
    // given
    List<SamlUser> samlUsers = new ArrayList<>();
    samlUsers.add(createSamlUser("dynamic user", 1));
    sut = new SamlUserStore(samlUsers);

    // when
    sut.addDynamicUser("dynamic user", "dynamic password");

    // then
    List<String> dynamicUsernames = sut.getSamlUsers().stream()
      .filter(samlUser -> samlUser.getUsername().equals("dynamic user"))
      .map(SamlUser::getUsername)
      .collect(Collectors.toList());

    assertThat(dynamicUsernames.size(), is(1));
  }

  @Test
  public void shouldOnlyRemoveDynamicUsers() {
    // given
    List<SamlUser> seedUsers = new ArrayList<>();
    seedUsers.add(createSamlUser("seed user", 1));
    sut = new SamlUserStore(seedUsers);
    sut.addDynamicUser("dynamic user", "dynamic password");

    // when
    sut.resetDynamicUsers();

    // then
    boolean dynamicUserMissing = sut.getSamlUsers().stream()
      .noneMatch(samlUser -> samlUser.getUsername().equals("dynamic user"));
    assertThat(dynamicUserMissing, is(true));

    boolean seedUserExists = sut.getSamlUsers().stream()
      .anyMatch(samlUser -> samlUser.getUsername().equals("seed user"));
    assertThat(seedUserExists, is(true));
  }

  @Test
  public void shouldResetAllSeedDataUserAttributes() {
    // given
    List<SamlUser> seedUsers = new ArrayList<>();
    seedUsers.add(createSamlUser("user 1", 2));
    sut = new SamlUserStore(seedUsers);
    sut.addDynamicUser("dynamic user", "dynamic password");

    // when
    sut.resetSeedDataUserAttributes();

    // then
    assertThat(sut.getUserAttributes("user 1").size(), is(0));
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
