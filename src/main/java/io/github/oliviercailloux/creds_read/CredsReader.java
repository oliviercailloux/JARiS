package io.github.oliviercailloux.creds_read;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
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
 * Immutable.
 * </p>
 * <p>
 * Allows to read the credentials: username and password. For each piece of
 * information, distinguishes <em>missing information</em> and <em>empty
 * string</em>. Considers the following possible sources (displayed here by
 * order of priority).
 * </p>
 * <ol>
 * <li>Properties {@value #usernameKey} and {@value #passwordKey}. Each property
 * may be set, including to the empty string, or not set. An information is
 * considered missing (from the properties source) iff the corresponding
 * property is not set.</li>
 * <li>Environment variables {@value #usernameKey} and {@value #passwordKey}.
 * Each variable may be set, including to the empty string, or not set. An
 * information is considered missing (from the environment variables source) iff
 * the corresponding environment variable is not set.</li>
 * <li>File {@value #filePath}. The two pieces of information are considered
 * missing (from the files source) iff the file does not exist. If the file
 * exists, no piece of information is considered missing. The first line of the
 * file gives the username, the second one gives the password. If the file has
 * only one line, the password (from the files source) is set to the empty
 * string. If the file is empty, both pieces of information (from the files
 * source) are set to the empty string. Empty lines are not considered at all.
 * If the file has non empty line content after the second line, it is an
 * error.</li>
 * </ol>
 * <p>
 * Best login information: The source used to return information is the one that has the highest
 * informational value, as determined by
 * {@link CredsOpt#getInformationalValue()} (meaning that sources are ordered by
 * increasing number of pieces of information missing), and, in case of ex-æquo,
 * the order of priority displayed in the previous paragraph determines which
 * source wins.
 * </p>
 * </p>
 */
public class CredsReader {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CredsReader.class);

	/**
	 * The default value of the username key used in
	 * <code>CredsReader.defaultCreds()</code>.
	 */
	public static final String DEFAULT_USERNAME_KEY = "API_username";

	/**
	 * The default value of the password key used in
	 * <code>CredsReader.defaultCreds()</code>.
	 */
	public static final String DEFAULT_PASSWORD_KEY = "API_password";

	/**
	 * The default value of the file path used in
	 * <code>CredsReader.defaultCreds()</code>.
	 */
	public static final Path DEFAULT_FILE_NAME = Path.of("API_login.txt");

	private final String usernameKey;

	private final String passwordKey;

	private final Path filePath;

	Map<String, String> env = System.getenv();

	/**
	 * @param usernameKey the given environment value for the username key.
	 * @param passwordKey the given environment value for the password key.
	 * @param filePath    the given environment value for the file path.
	 * @return a CredsReader instance that will read from the given parameters.
	 */
	public static CredsReader given(String usernameKey, String passwordKey, Path filePath) {
		CredsReader credsReader = new CredsReader(usernameKey, passwordKey, filePath);
		return credsReader;
	}

	/**
	 * @return a CredsReader instance that will read from the default environment
	 *         values DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, DEFAULT_FILE_NAME.
	 */
	public static CredsReader defaultCreds() {
		CredsReader credsReader = new CredsReader(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, DEFAULT_FILE_NAME);
		return credsReader;
	}

	private CredsReader(String usernameKey, String passwordKey, Path filePath) {
		this.usernameKey = checkNotNull(usernameKey);
		this.passwordKey = checkNotNull(passwordKey);
		this.filePath = checkNotNull(filePath);
	}

	public String getUsernameKey() {
		return usernameKey;
	}

	public String getPasswordKey() {
		return passwordKey;
	}

	public Path getFilePath() {
		return filePath;
	}

	/**
	 * @return the best login information found, or an exception.
	 * @throws IllegalStateException if information is missing from all three sources.
	 * @throws UncheckedIOException if an I/O error occurs reading from the file or a malformed orunmappable byte sequence is read.
	 * @see CredsReader
	 */
	public Credentials getCredentials() throws IllegalStateException, UncheckedIOException {
		final CredsOpt credsOpt;
		try {
			credsOpt = readCredentials();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		if (credsOpt.getUsername().isEmpty() && credsOpt.getPassword().isEmpty()) {
			throw new IllegalStateException("Login information not found.");
		}
		if (credsOpt.getUsername().isEmpty()) {
			throw new IllegalStateException("Found password but no username.");
		}
		if (credsOpt.getPassword().isEmpty()) {
			throw new IllegalStateException("Found username but no password.");
		}
		final Credentials credentials = Credentials.given(credsOpt.getUsername().get(), credsOpt.getPassword().get());
		return credentials;
	}

	/**
	 * <p>
	 * Returns the best authentication information it could find, throwing no error
	 * if some is missing.
	 * </p>
	 *
	 * @throws IllegalStateException if a file source is provided but has non empty
	 *                               line content after the second line.
	 * @see CredsReader
	 * @see CredsOpt
	 * 
	 */
	CredsOpt readCredentials() throws IOException, IllegalStateException {
		final CredsOpt propertyAuthentication;
		{
			final String username = System.getProperty(this.usernameKey);
			final String password = System.getProperty(this.passwordKey);
			propertyAuthentication = CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = propertyAuthentication.getInformationalValue();
			LOGGER.info(
					"Found {} piece" + (informationalValue >= 2 ? "s" : "") + " of login information in properties.",
					informationalValue);
		}

		final CredsOpt envAuthentication;
		{
			final String username = env.get(this.usernameKey);
			final String password = env.get(this.passwordKey);
			envAuthentication = CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = envAuthentication.getInformationalValue();
			LOGGER.info("Found {} piece" + (informationalValue >= 2 ? "s" : "")
					+ " of login information in environment variables.", informationalValue);
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
			LOGGER.info("Found {} piece" + (informationalValue >= 2 ? "s" : "") + " of login information in file.",
					informationalValue);
		}

		final TreeMap<Double, CredsOpt> map = new TreeMap<>();
		map.put(propertyAuthentication.getInformationalValue() * 1.2d, propertyAuthentication);
		map.put(envAuthentication.getInformationalValue() * 1.1d, envAuthentication);
		map.put(fileAuthentication.getInformationalValue() * 1.0d, fileAuthentication);
		return map.lastEntry().getValue();
	}

}
