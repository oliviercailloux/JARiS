package io.github.oliviercailloux.jaris;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * <p>
 * Immutable.
 * </p>
 * <p>
 * Stores two pieces of login information as String: username and password. Each
 * of these values can be an empty String but never <code>null</code>.
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

	@Override
	public boolean equals(Object o2) {
		if (!(o2 instanceof Credentials)) {
			return false;
		}
		final Credentials c2 = (Credentials) o2;
		return username.equals(c2.username) && password.equals(c2.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, password);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("username", username)
				.add("password", password.isEmpty() ? "" : "****").toString();
	}

}
