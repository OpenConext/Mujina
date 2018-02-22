package mujina.idp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class PermittedUrlComposerTest {

  private PermittedUrlComposer sut;

  @Test
  public void shouldReturnApiPathInPermittedUrlsWhenEnabled() {
    // given
    sut = new PermittedUrlComposer(true);

    // when
    String[] permittedUrls = sut.getPermittedUrls();

    // then
    assertThat("/api should be included", permittedUrls, is(new String[]{"/", "/metadata", "/favicon.ico", "/laa_logo.png", "/*.css", "/api/**"}));
  }

  @Test
  public void shouldNotReturnApiPathInPermittedUrlsWhenDisabled() {
    // given
    sut = new PermittedUrlComposer(false);

    // when
    String[] permittedUrls = sut.getPermittedUrls();

    // then
    assertThat("/api should not be included", permittedUrls, is(new String[]{"/", "/metadata", "/favicon.ico", "/laa_logo.png", "/*.css"}));
  }

  @Test
  public void shouldReturnApiPathInDeniedUrlsWhenDisabled() {
    // given
    sut = new PermittedUrlComposer(false);

    // when
    String[] deniedUrls = sut.getDeniedUrls();

    // then
    assertThat("/api should be included", deniedUrls, is(new String[]{"/api/**"}));
  }

  @Test
  public void shouldNotReturnApiPathInDeniedUrlsWhenEnabled() {
    // given
    sut = new PermittedUrlComposer(true);

    // when
    String[] deniedUrls = sut.getDeniedUrls();

    // then
    assertThat("/api should not be included", deniedUrls, is(new String[]{}));
  }
}
