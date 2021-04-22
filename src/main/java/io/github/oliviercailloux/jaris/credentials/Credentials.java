package io.github.oliviercailloux.jaris.credentials;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * <p>
 * Represents credentials of a user, meaning, a user’s login information, that is, a username and a
 * password.
 * </p>
 * <p>
 * This immutable object stores two pieces of login information as {@code String}: {@code username}
 * and {@code password}. Each of these values can be an empty {@code String}, but are never
 * {@code null}.
 * </p>
 */
public class Credentials {

  /**
   * Creates an instance containing the given information.
   *
   * @param username may be empty, but not {@code null}.
   * @param password may be empty, but not {@code null}.
   * @return an instance representing the given login information.
   */
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
   * Returns the username.
   *
   * @return may be an empty {@code String}, but never {@code null}.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the password.
   *
   * @return may be an empty {@code String}, but never {@code null}.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Returns {@code true} iff the given object is a {@link Credentials} and the username and
   * password match.
   */
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

  /**
   * Returns a string representation of this object, suitable for debug. The password is not
   * disclosed in the returned string, unless the password is the empty string.
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("username", username)
        .add("password", password.isEmpty() ? "" : "****").toString();
  }

}
