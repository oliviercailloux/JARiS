package io.github.oliviercailloux.creds_read;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

/**
 * <p>
 * Immutable.
 * </p>
 * <p>
 * Stores two Strings of login information: username and password. The value can
 * be an empty String but never <code>null</code>.
 * </p>
 */
public class Credentials {

	public static Credentials given(String username, String password) {
		Credentials authentication = new Credentials(username, password);
		return authentication;
	}

	private final String username;
	private final String password;

	private Credentials(String username, String password) {
		this.username = checkNotNull(username);
		this.password = checkNotNull(password);
	}

	/**
	 * 
	 * @return The value of the username which can be an empty String but never
	 *         <code>null</code>.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * 
	 * @return The value of the password which can be an empty String but never
	 *         <code>null</code>.
	 */
	public String getPassword() {
		return password;
	}

}
