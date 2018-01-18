package mujina.idp.user;

import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonMap;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class SamlUserTest {
  private SamlUser sut;

  @Test
  public void shouldHaveEmptyAttributesMap() {
    // given
    SamlUser sut = new SamlUser();

    // when
    // then
    assertThat(sut.getUsername(), is(nullValue()));
    assertThat(sut.getPassword(), is(nullValue()));
    assertThat(sut.getSamlAttributes(), is(Collections.emptyMap()));
  }

  @Test
  public void shouldReturnCorrectValuesFromMutators() {
    // given
    sut = new SamlUser();

    // when
    sut.setUsername("test user");
    sut.setPassword("test password");

    // then
    assertThat(sut.getUsername(), is("test user"));
    assertThat(sut.getPassword(), is("test password"));
  }

  @Test
  public void shouldReturnCorrectValuesFromConstructor() {
    // given
    sut = new SamlUser("test user", "test password", singletonMap("attribute 1", "value 1"));

    // when
    String toString = sut.toString();

    // then
    assertThat(toString, is(notNullValue()));
    assertThat(sut.getUsername(), is("test user"));
    assertThat(sut.getPassword(), is("test password"));
    assertThat(sut.getSamlAttributes().get("attribute 1"), is("value 1"));
  }
}
