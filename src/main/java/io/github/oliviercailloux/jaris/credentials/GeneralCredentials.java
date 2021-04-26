package io.github.oliviercailloux.jaris.credentials;

import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Represents credentials of a user.
 * </p>
 * <p>
 * This immutable object stores pieces of credential information as {@code String}. Each of these
 * values can be an empty {@code String}, but are never {@code null}.
 * </p>
 */
public class GeneralCredentials<K extends Enum<K>> {

  /**
   * Creates an instance containing the given information.
   *
   * @param username may be empty, but not {@code null}.
   * @param password may be empty, but not {@code null}.
   * @return an instance representing the given login information.
   */
  public static <K extends Enum<K>> GeneralCredentials<K> given(Map<K, String> credentials) {
    return new GeneralCredentials<>(credentials);
  }

  private final Map<K, String> credentials;

  private GeneralCredentials(Map<K, String> credentials) {
    this.credentials = Collections.unmodifiableMap(new EnumMap<>(credentials));
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
   * Returns {@code true} iff the given object is a {@link GeneralCredentials} and the username and
   * password match.
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof GeneralCredentials)) {
      return false;
    }
    final GeneralCredentials c2 = (GeneralCredentials) o2;
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
