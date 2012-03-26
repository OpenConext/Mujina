package jcox.saml;

import jcox.spring.User;

import org.opensaml.saml2.core.Response;
import org.springframework.security.core.AuthenticationException;

public interface AssertionConsumer {

	User consume(Response samlResponse) throws AuthenticationException;

}
