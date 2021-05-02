package io.github.oliviercailloux.jaris.credentials;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.github.oliviercailloux.jaris.exceptions.Unchecker.IO_UNCHECKER;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import io.github.oliviercailloux.jaris.collections.ImmutableCompleteMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class permits to read a user’s credentials (authentication information) from various
 * sources.
 * <p>
 * This object is associated with an ordered set of {@code keys} and to a {@code filePath}. It will
 * attempt to read credentials from several sources in turn.
 * </p>
 * <ul>
 * <li>This object will read credentials from the system properties if all the keys in {@code keys}
 * exist as system properties. If some keys exist as system properties but not all, it will throw an
 * exception as this is probably a configuration problem.</li>
 * <li>If none of the keys exist as system properties, it will attempt to read credentials from
 * environment variables. Similarly, if they are all set, it returns credentials read from there; if
 * some but not all are set, it throws.</li>
 * <li>Finally, if no keys exist in the system properties or environment variables, it reads the
 * file {@code filePath}. Each line of the file provides the information corresponding to one key
 * from {@code keys}, in order. All usual line terminators are recognized: CR+LF, LF, and CR. The
 * file is considered UTF-8 encoded. If the file has more lines than there are keys in {@code keys},
 * it throws an exception.</li>
 * </ul>
 * <p>
 * Note that a piece of information may be provided and empty: a system property or environment
 * variable may exist and be empty; and the file may exist and contain fewer lines than keys in
 * {@code keys} or contain empty lines.
 * </p>
 * <p>
 * This object will read from system properties and environment variables named according to
 * {@code keys}, transformed to string using {@link Object#toString()}. It is suggested to use
 * uppercase to follow the usual <a href=
 * "https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap08.html">convention</a> about
 * environment variables.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 * <p>
 * Reading from the file throws {@link UncheckedIOException} instead of {@link IOException} because
 * the file is considered as under control of the developer using this class, not of an end-user,
 * and thus it is reasonable to assume that the developer does not want to be resilient to failures
 * of the file system or to unexpected file format.
 * </p>
 * <h2>Usage</h2> If you are happy to use one of the default configurations, use
 * {@link #keyReader()} or {@link #classicalReader()}. Here is an example use if you want to use
 * your own keys.
 * <p>
 *
 * <pre>
 * {@code
 * public static enum MyOwnCredentialKeys {
 *   MY_FIRST_KEY, MY_SECOND_KEY, MY_THIRD_KEY
 * }
 *
 * CredentialsReader<MyOwnCredentialKeys> reader =
 *     CredentialsReader.using(MyOwnCredentials.class, Path.of("my file.txt"));
 * ImmutableCompleteMap<MyOwnCredentialKeys, String> credentials = reader.getCredentials();
 * String valueReadCorrespondingToMyFirstKey = credentials.get(MyOwnCredentialKeys.MY_FIRST_KEY);
 * // and so on for other keys.
 * }
 * </pre>
 *
 * See also the
 * <a href="https://github.com/oliviercailloux/JARiS/blob/master/README.adoc">readme</a> of this
 * library.
 * </p>
 */
public class CredentialsReader<K extends Enum<K>> {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsReader.class);

  public static enum KeyCredential {
    API_KEY
  }
  public static enum ClassicalCredentials {
    API_USERNAME, API_PASSWORD
  }

  /**
   * The default value of the file path.
   */
  public static final Path DEFAULT_FILE_PATH = Path.of("API_credentials.txt");

  private final Class<K> keysType;
  private final Path filePath;

  Map<String, String> env = System.getenv();

  /**
   * Returns an instance that will read from the sources configured with the given parameters.
   *
   * @param keysType the keys to use for reading from system properties and the environment.
   * @param filePath the file path to use for reading from the file source.
   * @return a configured instance.
   * @see #keyReader()
   * @see #classicalReader()
   */
  public static <K extends Enum<K>> CredentialsReader<K> using(Class<K> keysType, Path filePath) {
    return new CredentialsReader<>(keysType, filePath);
  }

  /**
   * Returns an instance reading from the key value {@link KeyCredential#API_KEY}, and the default
   * file {@link #DEFAULT_FILE_PATH}.
   *
   * @return a default instance.
   * @see #using(Class, Path)
   */
  public static CredentialsReader<KeyCredential> keyReader() {
    return new CredentialsReader<>(KeyCredential.class, DEFAULT_FILE_PATH);
  }

  /**
   * Returns an instance reading from the key values {@link ClassicalCredentials#API_USERNAME} and
   * {@link ClassicalCredentials#API_PASSWORD}, and the default file {@link #DEFAULT_FILE_PATH}.
   *
   * @return a default instance.
   * @see #using(Class, Path)
   */
  public static CredentialsReader<ClassicalCredentials> classicalReader() {
    return new CredentialsReader<>(ClassicalCredentials.class, DEFAULT_FILE_PATH);
  }

  private CredentialsReader(Class<K> keysType, Path filePath) {
    this.keysType = checkNotNull(keysType);
    this.filePath = checkNotNull(filePath);
  }

  /**
   * Returns the keys that this object is configure to read from the system properties and the
   * environment.
   */
  public Class<K> getKeysType() {
    return keysType;
  }

  /**
   * Returns the file path that is read from when considering the file source.
   */
  public Path getFilePath() {
    return filePath;
  }

  /**
   * Returns the credentials read from the first source containing credentials.
   *
   * @throws NoSuchElementException if no credentials are found: {@code keys} is not empty, none of
   *         these keys exist as system properties or environment variables, and the source file
   *         does not exist
   * @throws IllegalStateException if some keys, but not all, exist as system properties or
   *         environment variables, or if the file source has non empty line content after the
   *         <i>n</i>th line, where <i>n</i> is the number of {@code keys}.
   * @throws UncheckedIOException if an I/O error occurs reading from the file or a malformed or
   *         unmappable byte sequence is read from the file.
   * @see CredentialsReader
   */
  public ImmutableCompleteMap<K, String> getCredentials()
      throws IllegalStateException, UncheckedIOException {
    final ImmutableSet<K> keys = ImmutableSet.copyOf(keysType.getEnumConstants());
    {
      final ImmutableMap.Builder<K, String> builder = ImmutableMap.builder();
      for (K key : keys) {
        final Optional<String> info = Optional.ofNullable(System.getProperty(key.toString()));
        LOGGER.info("Got {} from {}.", info, key);
        info.ifPresent(s -> builder.put(key, s));
      }
      final ImmutableMap<K, String> credentials = builder.build();

      final int size = credentials.size();
      if (size > 0 && size < keys.size()) {
        throw new IllegalStateException(
            "Partial credential information found in system properties: " + credentials.keySet()
                + ", missing: " + Sets.difference(keys, credentials.keySet()));
      }

      if (credentials.keySet().equals(keys)) {
        LOGGER.info("Found credentials in system properties {}.", keys);
        return ImmutableCompleteMap.fromEnumType(keysType, credentials);
      }
    }

    {
      final ImmutableMap.Builder<K, String> builder = ImmutableMap.builder();
      for (K key : keys) {
        final Optional<String> info = Optional.ofNullable(env.get(key.toString()));
        info.ifPresent(s -> builder.put(key, s));
      }
      final ImmutableMap<K, String> credentials = builder.build();

      final int size = credentials.size();
      if (size > 0 && size < keys.size()) {
        throw new IllegalStateException(
            "Partial credential information found in environment variables: " + credentials.keySet()
                + ", missing: " + Sets.difference(keys, credentials.keySet()));
      }

      if (credentials.keySet().equals(keys)) {
        LOGGER.info("Found credentials in environment variables {}.", keys);
        return ImmutableCompleteMap.fromEnumType(keysType, credentials);
      }
    }

    {
      if (!Files.exists(filePath)) {
        throw new NoSuchElementException("No credential information found (searching in keys "
            + keys + " and in file " + filePath + ").");
      }

      final List<String> lines = IO_UNCHECKER.getUsing(() -> Files.readAllLines(filePath));
      final List<String> supplementaryLines = lines.size() < keys.size() ? ImmutableList.of()
          : lines.subList(keys.size(), lines.size());
      checkState(supplementaryLines.stream().allMatch(s -> s.equals("")), "File " + filePath
          + " is too long: it has non-empty content after line number " + keys.size() + ".");
      final ImmutableMap.Builder<K, String> builder = ImmutableMap.builder();
      Streams.forEachPair(keys.stream(), Stream.concat(lines.stream(), Stream.generate(() -> "")),
          builder::put);
      final ImmutableMap<K, String> credentials = builder.build();

      LOGGER.info("Found credentials in file {}.", filePath);
      return ImmutableCompleteMap.fromEnumType(keysType, credentials);
    }
  }

  /**
   * Returns {@code true} iff the given object also is a credentials reader and is configured to
   * read from the same sources (meaning, key values and file path).
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof CredentialsReader)) {
      return false;
    }
    final CredentialsReader<?> t2 = (CredentialsReader<?>) o2;
    return keysType.equals(t2.keysType) && filePath.equals(t2.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keysType, filePath);
  }

  /**
   * Returns a short debug string representing this object’s state.
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("Keys type", keysType).add("File path", filePath)
        .toString();
  }
}
