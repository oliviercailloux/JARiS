package io.github.oliviercailloux.jaris.credentials;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.io.CharSource;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
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
 * This object is associated with a class {@code K} and to a {@code source}. The class {@code K}
 * must have a unique public constructor whose parameters are solely of type {@code String} (it may
 * be the implicit constructor of a record). The parameters of that constructor determine the
 * ordered set of {@code keys} associated with this object. This object will attempt to read
 * credentials associated to these keys from several sources in turn.
 * </p>
 * <ul>
 * <li>This object will read credentials from the system properties if all the keys in {@code keys}
 * exist as system properties. If some keys exist as system properties but not all, it will throw an
 * exception as this is probably a configuration problem.</li>
 * <li>If none of the keys exist as system properties, it will attempt to read credentials from
 * environment variables. Similarly, if they are all set, it returns credentials read from there; if
 * some but not all are set, it throws.</li>
 * <li>Finally, if no keys exist in the system properties or environment variables, it reads the
 * given {@code source}. Each line of the source content provides the information corresponding to
 * one key from {@code keys}, in order. All usual line terminators are recognized: CR+LF, LF, and
 * CR. If the source has more lines than there are keys in {@code keys}, and some of these lines are
 * not empty, it throws an exception.</li>
 * </ul>
 * <p>
 * Note that a piece of information may be provided and empty: a system property or environment
 * variable may exist and be empty; and the source may exist and contain fewer lines than keys in
 * {@code keys} or contain empty lines.
 * </p>
 * <p>
 * As for {@code keys}, it is suggested to use uppercase to follow the usual <a href=
 * "https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap08.html">convention</a> about
 * environment variables.
 * </p>
 * <p>
 * Instances of this class are immutable.
 * </p>
 * <p>
 * Reading from the source throws {@link UncheckedIOException} instead of {@link IOException}
 * because the source is considered as under control of the developer using this class, not of an
 * end-user, and thus it is reasonable to assume that the developer does not want to be resilient to
 * failures of the file system or to unexpected file format.
 * </p>
 * <h2>Usage</h2> If you are happy to use one of the default configurations, use
 * {@link #keyReader()} or {@link #classicalReader()}. Here is an example use if you want to use
 * your own keys.
 *
 * <pre>
 * {@code
 * public record MyOwnCredentials(String MY_FIRST_KEY, String MY_SECOND_KEY, String MY_THIRD_KEY) {
 * }
 *
 * CredentialsReader<MyOwnCredentials> reader =
 *   CredentialsReader.using(MyOwnCredentials.class,
 *     MoreFiles.asCharSource("my file.txt", StandardCharsets.UTF_8));
 * MyOwnCredentials credentials = reader.getCredentials();
 * String valueReadCorrespondingToMyFirstKey = credentials.MY_FIRST_KEY();
 * // and so on for other keys.
 * }
 * </pre>
 * <p>
 * See also the <a href="https://github.com/oliviercailloux/JARiS/blob/main/README.adoc">README</a>
 * of this library.
 * </p>
 */
public class CredentialsReader<K> {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsReader.class);

  /**
   * The file path for the default credentials source.
   */
  public static final Path DEFAULT_FILE_PATH = Path.of("API_credentials.txt");
  private static final CharSource DEFAULT_SOURCE =
      MoreFiles.asCharSource(DEFAULT_FILE_PATH, StandardCharsets.UTF_8);

  private final Class<K> keysType;
  private final CharSource source;

  private final Constructor<K> constructor;

  private final ImmutableSet<Parameter> parameters;

  Map<String, String> env = System.getenv();

  /**
   * Returns an instance that will read from the sources configured with the given parameters.
   *
   * @param <K> the type of keys used by the returned instance
   * @param keysType the keys to use for reading from system properties and the environment.
   * @param source the credentials source.
   * @return a configured instance.
   * @see #keyReader()
   * @see #classicalReader()
   */
  public static <K> CredentialsReader<K> using(Class<K> keysType, CharSource source) {
    return new CredentialsReader<>(keysType, source);
  }

  /**
   * Returns an instance reading from the key value {@link KeyCredential#API_KEY}, and the default
   * source {@link #DEFAULT_FILE_PATH}.
   *
   * @return a default instance.
   * @see #using(Class, CharSource)
   */
  public static CredentialsReader<KeyCredential> keyReader() {
    return new CredentialsReader<>(KeyCredential.class, DEFAULT_SOURCE);
  }

  /**
   * Returns an instance reading from the key values {@link Credentials#API_USERNAME} and
   * {@link Credentials#API_PASSWORD}, and the default source {@link #DEFAULT_FILE_PATH}.
   *
   * @return a default instance.
   * @see #using(Class, CharSource)
   */
  public static CredentialsReader<Credentials> classicalReader() {
    return new CredentialsReader<>(Credentials.class, DEFAULT_SOURCE);
  }

  private CredentialsReader(Class<K> keysType, CharSource source) {
    this.keysType = checkNotNull(keysType);
    this.source = checkNotNull(source);

    final ImmutableSet<Constructor<?>> suitableConstructors = ImmutableSet
        .copyOf(keysType.getDeclaredConstructors()).stream().filter(c -> c.canAccess(null))
        .filter(c -> ImmutableSet.copyOf(c.getParameters()).stream()
            .allMatch(p -> p.getType().equals(String.class)))
        .collect(ImmutableSet.toImmutableSet());
    checkArgument(suitableConstructors.size() == 1);
    final Constructor<?> nonTypedconstructor = Iterables.getOnlyElement(suitableConstructors);
    parameters = ImmutableSet.copyOf(nonTypedconstructor.getParameters());
    verify(parameters.stream().allMatch(p -> p.getType().equals(String.class)));
    try {
      constructor = keysType.getDeclaredConstructor(
          parameters.stream().map(Parameter::getType).toList().toArray(new Class[] {}));
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
    verify(constructor.equals(nonTypedconstructor));
  }

  /**
   * Returns the keys that this object is associated with.
   *
   * @return the keys
   */
  public Class<K> getKeysType() {
    return keysType;
  }

  /**
   * Returns the credentials read from the first source containing credentials.
   *
   * @return the credentials
   * @throws NoSuchElementException if no credentials are found: {@code keys} is not empty, none of
   *         these keys exist as system properties or environment variables, and the source does not
   *         exist
   * @throws IllegalStateException if some keys, but not all, exist as system properties or
   *         environment variables, or if the given source has non empty line content after the
   *         <i>n</i>th line, where <i>n</i> is the number of {@code keys}.
   * @throws UncheckedIOException if an I/O error occurs reading from the given source or a
   *         malformed or unmappable byte sequence is read from the given source.
   * @see CredentialsReader
   */
  public K getCredentials() throws IllegalStateException, UncheckedIOException {
    try {
      return constructor.newInstance(getCredentialsByParam().values().toArray());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  private ImmutableMap<Parameter, String> getCredentialsByParam()
      throws IllegalStateException, UncheckedIOException {
    final ImmutableMap.Builder<Parameter, String> builder = ImmutableMap.builder();
    {
      for (Parameter p : parameters) {
        final Optional<String> info = Optional.ofNullable(System.getProperty(p.getName()));
        info.ifPresent(s -> builder.put(p, s));
      }
      final ImmutableMap<Parameter, String> credentials = builder.build();

      final int size = credentials.size();
      if (size > 0 && size < parameters.size()) {
        throw new IllegalStateException(
            "Partial credential information found in system properties: " + credentials.keySet()
                + ", missing: " + Sets.difference(parameters, credentials.keySet()));
      }

      if (credentials.keySet().equals(parameters)) {
        LOGGER.info("Found credentials in system properties {}.", parameters);
        return credentials;
      }
    }

    {
      for (Parameter p : parameters) {
        final Optional<String> info = Optional.ofNullable(env.get(p.getName()));
        info.ifPresent(s -> builder.put(p, s));
      }
      final ImmutableMap<Parameter, String> credentials = builder.build();

      final int size = credentials.size();
      if (size > 0 && size < parameters.size()) {
        throw new IllegalStateException(
            "Partial credential information found in environment variables: " + credentials.keySet()
                + ", missing: " + Sets.difference(parameters, credentials.keySet()));
      }

      if (credentials.keySet().equals(parameters)) {
        LOGGER.info("Found credentials in environment variables {}.", parameters);
        return credentials;
      }
    }

    {

      ImmutableList<String> lines;
      try {
        lines = source.readLines();
      } catch (NoSuchFileException e) {
        throw new NoSuchElementException("No credential information found (searching for keys "
            + parameters + " in system properties, in environment, and in source " + source + ").",
            e);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      final List<String> supplementaryLines = lines.size() < parameters.size() ? ImmutableList.of()
          : lines.subList(parameters.size(), lines.size());
      checkState(supplementaryLines.stream().allMatch(s -> s.equals("")), "Source content " + source
          + " is too long: it has non-empty content after line number " + parameters.size() + ".");
      Streams.forEachPair(parameters.stream(),
          Stream.concat(lines.stream(), Stream.generate(() -> "")), builder::put);
      final ImmutableMap<Parameter, String> credentials = builder.build();

      LOGGER.info("Found credentials in source {}.", source);
      return credentials;
    }
  }

  /**
   * Returns {@code true} iff the given object also is a credentials reader and is configured to
   * read the same keys from the same sources (meaning that it has equal keysTypes and source).
   */
  @Override
  public boolean equals(Object o2) {
    if (!(o2 instanceof CredentialsReader)) {
      return false;
    }
    final CredentialsReader<?> t2 = (CredentialsReader<?>) o2;
    return keysType.equals(t2.keysType) && source.equals(t2.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keysType, source);
  }

  /**
   * Returns a short debug string representing this object’s state.
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("Keys type", keysType).add("Source", source)
        .toString();
  }
}
