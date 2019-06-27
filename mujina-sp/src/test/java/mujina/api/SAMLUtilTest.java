package mujina.api;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import org.springframework.security.saml.util.SAMLUtil;

import static org.junit.Assert.assertFalse;

public class SAMLUtilTest {

  @Test
  public void isDateTimeSkewValid() {
    DateTime dateTime = new DateTime();
    DateTime future = dateTime.withDurationAdded(Duration.standardDays(1), 1);
    boolean dateTimeSkewValid = SAMLUtil.isDateTimeSkewValid(60, 7200, future);
    assertFalse(dateTimeSkewValid);
  }

}
