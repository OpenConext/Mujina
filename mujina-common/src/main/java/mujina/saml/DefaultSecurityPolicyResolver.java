package mujina.saml;

import org.opensaml.common.binding.security.IssueInstantRule;
import org.opensaml.common.binding.security.MessageReplayRule;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.util.storage.ReplayCache;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.StaticSecurityPolicyResolver;

import java.util.Arrays;

public class DefaultSecurityPolicyResolver extends StaticSecurityPolicyResolver {

  private static BasicSecurityPolicy securityPolicy;

  static {
    securityPolicy = new BasicSecurityPolicy();
    securityPolicy.getPolicyRules().addAll(Arrays.asList(new IssueInstantRule(90, 300),
      new MessageReplayRule(new ReplayCache(new MapBasedStorageService(), 14400000))));
  }

  public DefaultSecurityPolicyResolver() {
    super(securityPolicy);
  }

}
