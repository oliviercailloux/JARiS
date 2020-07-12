package io.github.oliviercailloux.jaris;

import static io.github.oliviercailloux.jaris.CredsReader.DEFAULT_FILE_NAME;
import static io.github.oliviercailloux.jaris.CredsReader.DEFAULT_PASSWORD_KEY;
import static io.github.oliviercailloux.jaris.CredsReader.DEFAULT_USERNAME_KEY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;

import io.github.oliviercailloux.jaris.CredsOpt;
import io.github.oliviercailloux.jaris.CredsReader;

class CredsReaderTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CredsReaderTests.class);
	private FileSystem jimfs;

	@BeforeEach
	void initJimFs() {
		jimfs = Jimfs.newFileSystem();
	}

	Path createApiLoginFile(String... lines) throws IOException {
		Path filePath = jimfs.getPath(DEFAULT_FILE_NAME.toString());
		/** If lines is empty, an empty file gets created. */
		Files.write(filePath, ImmutableList.copyOf(lines));
		return filePath;
	}

	Path getNonExistentFile() {
		return jimfs.getPath("Nonexistent.txt");
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of();

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropAndEnvReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username", DEFAULT_PASSWORD_KEY, "env password");

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testHalfEnvAndPropReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username");

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testHalfPropAndEnvReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username", DEFAULT_PASSWORD_KEY, "env password");

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testHalfPropAndHalfEnvAndFullFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password"));
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username");

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testHalfPropHalfEnvAndFileReadCredentials() throws IOException {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password"));
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username");

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropEnvAndFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password"));
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username", DEFAULT_PASSWORD_KEY, "env password");

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "")
	@SetSystemProperty(key = DEFAULT_PASSWORD_KEY, value = "")
	@Test
	public void testPropSetToEmptyStringsReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of();

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("", myAuth.getUsername().get());
		assertEquals("", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testNoPasswordReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username");

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty(), myAuth.getPassword().toString());
	}

	@Test
	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	public void testFileReadCredentials() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username", "file password"));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username", "file password", ""));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username", "file password", "", ""));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
	}

	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testEmptyFile() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile());
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("", "", "", "", ""));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile());
			credsReader.env = Map.of();

			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("", "", "", "", ""));
			credsReader.env = Map.of();

			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
	}

	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testEmptyUsernameFile() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("", "file password"));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("", "file password"));
			credsReader.env = Map.of();

			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
	}

	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testEmptyPasswordFile() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username"));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username", ""));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username", "", ""));
			credsReader.env = Map.of();

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username"));
			credsReader.env = Map.of();

			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username", ""));
			credsReader.env = Map.of();

			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
		{
			CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
					createApiLoginFile("file username", "", "", ""));
			credsReader.env = Map.of();

			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
	}

	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testIncorrectFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password", "garbage"));
		credsReader.env = Map.of();

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.readCredentials());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testGarbageLaterFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password", "", "Garbage"));
		credsReader.env = Map.of();

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.readCredentials());
		assertEquals("File API_login.txt is too long: 4 lines", exception.getMessage());
	}

	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testNoneGetCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of();

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@ClearSystemProperty(key = DEFAULT_USERNAME_KEY)
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testHalfEnvGetCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username");

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@SetSystemProperty(key = DEFAULT_USERNAME_KEY, value = "prop username")
	@ClearSystemProperty(key = DEFAULT_PASSWORD_KEY)
	@Test
	public void testHalfPropAndHalfEnvGetCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, getNonExistentFile());
		credsReader.env = Map.of(DEFAULT_USERNAME_KEY, "env username");

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@AfterEach
	void closeJimFs() throws IOException {
		jimfs.close();
	}

}