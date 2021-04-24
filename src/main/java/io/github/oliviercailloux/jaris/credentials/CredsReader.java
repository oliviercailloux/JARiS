package io.github.oliviercailloux.jaris.credentials;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class permits to read a user’s credentials (authentication information), meaning, a username
 * and a password, from various sources.
 * </p>
 * <p>
 * Instances of this class will read from the following three sources of information, and return the
 * credentials from the first <em>valid</em> source it found (following the order of priority
 * displayed here). A source is valid iff it provides the two pieces of information required:
 * username and password. Be aware that a piece of information may be provided and empty.
 * </p>
 * <ol>
 * <li>System properties {@code usernameKey} and {@code passwordKey}. Each property may be set,
 * including to the empty string, or not set. This source is valid iff both properties are set.</li>
 * <li>Environment variables {@code usernameKey} and {@code passwordKey}. Each variable may be set,
 * including to the empty string, or not set. This source is valid iff both environment variables
 * are set.</li>
 * <li>File {@code filePath}. This source is valid iff the file exists. The first line of the file
 * gives the username, the second one gives the password. If the file has only one line, the
 * password is considered to be the empty string. If the file is empty, the username and the
 * password are both considered to be the empty string. Empty lines are not considered at all. If
 * the file has non empty line content after the second line, it is an error. Both classical end of
 * line marks ({@code \r} and {@code \r\n}) are considered as end of lines, and the file is
 * considered as encoded in UTF-8.</li>
 * </ol>
 * <p>
 * This class also permits to configure the sources that it attempts to read from, at instance
 * creation time (using the factory methods). If not provided, the default value for
 * {@code usernameKey} is {@value #DEFAULT_USERNAME_KEY}; the default value for {@code passwordKey}
 * is {@value #DEFAULT_PASSWORD_KEY} (both for system properties and environment); and the default
 * value for {@code filePath} is {@code "API_login.txt"} in the current directory.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 */
public class CredsReader {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(CredsReader.class);

  /**
   * The default value of the username key used in {@code CredsReader.defaultCreds()}. (All
   * uppercase to follow the usual <a href=
   * "https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap08.html">convention</a> when
   * used as an environment variable.)
   */
  public static final String DEFAULT_USERNAME_KEY = "API_USERNAME";

  /**
   * The default value of the password key used in {@code CredsReader.defaultCreds()}. (All
   * uppercase to follow the usual <a href=
   * "https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap08.html">convention</a> when
   * used as an environment variable.)
   */
  public static final String DEFAULT_PASSWORD_KEY = "API_PASSWORD";

  /**
   * The default value of the file path.
   */
  public static final Path DEFAULT_FILE_PATH = Path.of("API_login.txt");

  private final String usernameKey;

  private final String passwordKey;

  private final Path filePath;

  Map<String, String> env = System.getenv();

  /**
   * Returns an instance that will read from the sources configured with the given parameters.
   *
   * @param usernameKey the username key to use for reading from system properties and the
   *        environment.
   * @param passwordKey the password key to use for reading from system properties and the
   *        environment.
   * @param filePath the file path to use for reading from the file.
   * @return a configured instance.
   * @see #defaultCredsReader()
   */
  public static CredsReader given(String usernameKey, String passwordKey, Path filePath) {
    CredsReader credsReader = new CredsReader(usernameKey, passwordKey, filePath);
    return credsReader;
  }

  /**
   * Returns an instance that will use the default values {@link #DEFAULT_USERNAME_KEY},
   * {@link #DEFAULT_PASSWORD_KEY}, {@link #DEFAULT_FILE_PATH}.
   *
   * @return a default instance.
   * @see #given(String, String, Path)
   */
  public static CredsReader defaultCredsReader() {
    CredsReader credsReader =
        new CredsReader(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, DEFAULT_FILE_PATH);
    return credsReader;
  }

  private CredsReader(String usernameKey, String passwordKey, Path filePath) {
    this.usernameKey = checkNotNull(usernameKey);
    this.passwordKey = checkNotNull(passwordKey);
    this.filePath = checkNotNull(filePath);
  }

  /**
   * Returns the username key, used to read from the system properties and the environment.
   */
  public String getUsernameKey() {
    return usernameKey;
  }

  /**
   * Returns the password key, used to read from the system properties and the environment.
   */
  public String getPasswordKey() {
    return passwordKey;
  }

  /**
   * Returns the file path that is read from, considering the file source.
   */
  public Path getFilePath() {
    return filePath;
  }

  /**
   * Returns the credentials found from the first valid source, unless an exception is raised.
   *
   * @throws IllegalStateException if no valid source is found, or if the file source has non empty
   *         line content after the second line.
   * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable
   *         byte sequence is read from the file.
   * @see CredsReader
   */
  public Credentials getCredentials() throws IllegalStateException, IOException {
    final CredsOpt credsOpt = readCredentials();

    if (credsOpt.getUsername().isEmpty() && credsOpt.getPassword().isEmpty()) {
      throw new IllegalStateException("Login information not found.");
    }
    if (credsOpt.getUsername().isEmpty()) {
      throw new IllegalStateException("Found password but no username.");
    }
    if (credsOpt.getPassword().isEmpty()) {
      throw new IllegalStateException("Found username but no password.");
    }
    final Credentials credentials =
        Credentials.given(credsOpt.getUsername().get(), credsOpt.getPassword().get());
    return credentials;
  }

  /**
   * <p>
   * Returns the best credentials it could find, throwing no error if some is missing.
   * </p>
   *
   * @throws IllegalStateException if a file source is provided but has non empty line content after
   *         the second line.
   *
   */
  CredsOpt readCredentials() throws IOException, IllegalStateException {
    final CredsOpt propertyAuthentication;
    {
      final String username = System.getProperty(usernameKey);
      final String password = System.getProperty(passwordKey);
      propertyAuthentication =
          CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
      final int informationalValue = propertyAuthentication.getInformationalValue();
      LOGGER.info(
          "Found {} piece" + (informationalValue == 2 ? "s" : "")
              + " of login information in properties {} and {}.",
          informationalValue, usernameKey, passwordKey);
    }

    final CredsOpt envAuthentication;
    {
      final String username = env.get(usernameKey);
      final String password = env.get(passwordKey);
      envAuthentication =
          CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
      final int informationalValue = envAuthentication.getInformationalValue();
      LOGGER.info(
          "Found {} piece" + (informationalValue == 2 ? "s" : "")
              + " of login information in environment variables {} and {}.",
          informationalValue, usernameKey, passwordKey);
    }

    final CredsOpt fileAuthentication;
    {
      final Optional<String> optUsername;
      final Optional<String> optPassword;
      final Path path = filePath;
      if (!Files.exists(path)) {
        optUsername = Optional.empty();
        optPassword = Optional.empty();
      } else {
        final List<String> lines = Files.readAllLines(path);
        final Iterator<String> iterator = lines.iterator();
        if (iterator.hasNext()) {
          optUsername = Optional.of(iterator.next());
        } else {
          optUsername = Optional.of("");
        }
        if (iterator.hasNext()) {
          optPassword = Optional.of(iterator.next());
        } else {
          optPassword = Optional.of("");
        }
        while (iterator.hasNext()) {
          if (!iterator.next().isEmpty()) {
            throw new IllegalStateException(
                "File " + filePath + " is too long: " + lines.size() + " lines");
          }
        }
      }
      fileAuthentication = CredsOpt.given(optUsername, optPassword);
      final int informationalValue = fileAuthentication.getInformationalValue();
      LOGGER.info("Found {} piece" + (informationalValue == 2 ? "s" : "")
          + " of login information in file.", informationalValue);
    }

    final TreeMap<Double, CredsOpt> map = new TreeMap<>();
    map.put(propertyAuthentication.getInformationalValue() * 1.2d, propertyAuthentication);
    map.put(envAuthentication.getInformationalValue() * 1.1d, envAuthentication);
    map.put(fileAuthentication.getInformationalValue() * 1.0d, fileAuthentication);
    return map.lastEntry().getValue();
  }
}
