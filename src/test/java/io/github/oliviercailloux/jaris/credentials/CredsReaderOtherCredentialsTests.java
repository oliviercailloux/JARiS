package io.github.oliviercailloux.jaris.credentials;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;
import io.github.oliviercailloux.jaris.collections.ImmutableCompleteMap;
import io.github.oliviercailloux.jaris.credentials.CredentialsReader.KeyCredential;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CredsReaderOtherCredentialsTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER =
      LoggerFactory.getLogger(CredsReaderOtherCredentialsTests.class);

  private static final String API_KEY = "API_KEY";

  private FileSystem jimfs;

  @BeforeEach
  void initJimFs() {
    jimfs = Jimfs.newFileSystem();
  }

  Path createApiLoginFile(String... lines) throws IOException {
    Path filePath = jimfs.getPath("MyTestLoginFile.txt");
    /* If lines is empty, an empty file gets created. */
    Files.write(filePath, ImmutableList.copyOf(lines));
    return filePath;
  }

  Path getNonExistentFile() {
    return jimfs.getPath("Nonexistent.txt");
  }

  @SetSystemProperty(key = API_KEY, value = "prop key")
  @Test
  public void test1SysProp() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, getNonExistentFile());
    credsReader.env = Map.of();

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("prop key", myAuth.get(KeyCredential.API_KEY));
  }

  @SetSystemProperty(key = API_KEY, value = "prop key")
  @Test
  public void test1SysPropAnd1Env() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, getNonExistentFile());
    credsReader.env = Map.of(API_KEY, "env key");

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("prop key", myAuth.get(KeyCredential.API_KEY));
  }

  @SetSystemProperty(key = API_KEY, value = "prop key")
  @Test
  public void test1SysPropAnd1InFile() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, createApiLoginFile("file key"));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("prop key", myAuth.get(KeyCredential.API_KEY));
  }

  @SetSystemProperty(key = API_KEY, value = "prop key")
  @Test
  public void test1SysPropAnd2InFile() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, createApiLoginFile("file key", "spurious"));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("prop key", myAuth.get(KeyCredential.API_KEY));
  }

  @Test
  public void test0SysPropsAnd1Env() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, getNonExistentFile());
    credsReader.env = Map.of(API_KEY, "env key");

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("env key", myAuth.get(KeyCredential.API_KEY));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd0InFile() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, createApiLoginFile());
    credsReader.env = Map.of();

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("", myAuth.get(KeyCredential.API_KEY));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd1InFile() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, createApiLoginFile("file key"));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("file key", myAuth.get(KeyCredential.API_KEY));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd1InFileThen1EmptyLine() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, createApiLoginFile("file key", ""));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<KeyCredential, String> myAuth = credsReader.getCredentials();

    assertEquals("file key", myAuth.get(KeyCredential.API_KEY));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd2InFile() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, createApiLoginFile("file key", "spurious"));
    credsReader.env = Map.of();

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @Test
  public void test0SysPropsAnd0EnvAnd1EmptyLineThen1InFile() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, createApiLoginFile("", "spurious"));
    credsReader.env = Map.of();

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @Test
  public void test0SysPropsAnd0EnvAndNoFile() throws Exception {
    final CredentialsReader<KeyCredential> credsReader =
        CredentialsReader.using(KeyCredential.class, getNonExistentFile());
    credsReader.env = Map.of();

    assertThrows(NoSuchElementException.class, () -> credsReader.getCredentials());
  }

  private static enum EmptyEnum {
  }

  @Test
  public void testNoKeyNoFile() throws Exception {
    CredentialsReader<EmptyEnum> credsReader =
        CredentialsReader.using(EmptyEnum.class, getNonExistentFile());
    credsReader.env = Map.of();

    assertDoesNotThrow(() -> credsReader.getCredentials());
  }

  @Test
  public void testNoKeyAnd0InFile() throws Exception {
    CredentialsReader<EmptyEnum> credsReader =
        CredentialsReader.using(EmptyEnum.class, createApiLoginFile());
    credsReader.env = Map.of();

    assertDoesNotThrow(() -> credsReader.getCredentials());
  }

  @Test
  public void testNoKeyAnd1InFile() throws Exception {
    CredentialsReader<EmptyEnum> credsReader =
        CredentialsReader.using(EmptyEnum.class, createApiLoginFile("spurious"));
    credsReader.env = Map.of();

    assertDoesNotThrow(() -> credsReader.getCredentials());
  }

  @AfterEach
  void closeJimFs() throws IOException {
    jimfs.close();
  }
}
