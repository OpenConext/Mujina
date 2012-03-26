package nl.surfnet.mockoleth.spring;

import org.springframework.security.core.AuthenticationException;

public class ServiceProviderAuthenticationException extends
		AuthenticationException {

	public ServiceProviderAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

	public ServiceProviderAuthenticationException(String msg) {
		super(msg);
	}

}
