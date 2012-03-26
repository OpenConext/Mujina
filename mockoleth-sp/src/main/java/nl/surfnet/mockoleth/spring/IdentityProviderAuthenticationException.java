package nl.surfnet.mockoleth.spring;

import org.springframework.security.core.AuthenticationException;

public class IdentityProviderAuthenticationException extends AuthenticationException {

	public IdentityProviderAuthenticationException(String msg, Object extraInformation) {
		super(msg, extraInformation);
	}


	public IdentityProviderAuthenticationException(String msg) {
		super(msg);
	}

}
