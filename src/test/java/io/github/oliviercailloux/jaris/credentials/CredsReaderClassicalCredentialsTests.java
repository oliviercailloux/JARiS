package io.github.oliviercailloux.jaris.credentials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;
import io.github.oliviercailloux.jaris.collections.ImmutableCompleteMap;
import io.github.oliviercailloux.jaris.credentials.CredentialsReader.ClassicalCredentials;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClearSystemProperty(key = CredsReaderClassicalCredentialsTests.API_USERNAME)
@ClearSystemProperty(key = CredsReaderClassicalCredentialsTests.API_PASSWORD)
class CredsReaderClassicalCredentialsTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER =
      LoggerFactory.getLogger(CredsReaderClassicalCredentialsTests.class);

  static final String API_USERNAME = "API_USERNAME";

  static final String API_PASSWORD = "API_PASSWORD";

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

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @SetSystemProperty(key = API_PASSWORD, value = "prop password")
  @Test
  public void test2SysProps() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("prop username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("prop password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @SetSystemProperty(key = API_PASSWORD, value = "prop password")
  @Test
  public void test2SysPropsAnd2Env() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of(API_USERNAME, "env username", API_PASSWORD, "env password");

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("prop username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("prop password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @SetSystemProperty(key = API_PASSWORD, value = "prop password")
  @Test
  public void test2SysPropsAnd1Env() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of(API_USERNAME, "env username");

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("prop username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("prop password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @SetSystemProperty(key = API_PASSWORD, value = "prop password")
  @Test
  public void test2SysPropsAnd2InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader
        .using(ClassicalCredentials.class, createApiLoginFile("file username", "file password"));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("prop username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("prop password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @SetSystemProperty(key = API_PASSWORD, value = "prop password")
  @Test
  public void test2SysPropsAnd3InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class,
            createApiLoginFile("file username", "file password", "supplementary"));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("prop username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("prop password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @Test
  public void test1SysPropMissing1() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of();

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @Test
  public void test1SysPropMissing1And2Env() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of(API_USERNAME, "env username", API_PASSWORD, "env password");

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @SetSystemProperty(key = API_USERNAME, value = "prop username")
  @Test
  public void test1SysPropMissing1And2InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader
        .using(ClassicalCredentials.class, createApiLoginFile("file username", "file password"));
    credsReader.env = Map.of();

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @Test
  public void test0SysPropsAnd2Env() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of(API_USERNAME, "env username", API_PASSWORD, "env password");

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("env username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("env password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd1Env() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of(API_USERNAME, "env username");

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @Test
  public void test0SysPropsAnd1EnvAnd2InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader
        .using(ClassicalCredentials.class, createApiLoginFile("file username", "file password"));
    credsReader.env = Map.of(API_USERNAME, "env username");

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @Test
  public void test0SysPropsAnd0EnvAnd0InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, createApiLoginFile());
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd1InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, createApiLoginFile("file username"));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("file username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd1InFileThen1EmptyLine() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader
        .using(ClassicalCredentials.class, createApiLoginFile("file username", ""));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("file username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd1InFileThen2EmptyLines() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader
        .using(ClassicalCredentials.class, createApiLoginFile("file username", "", ""));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("file username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd1InFileThen3EmptyLines() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader
        .using(ClassicalCredentials.class, createApiLoginFile("file username", "", "", ""));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("file username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd2InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader
        .using(ClassicalCredentials.class, createApiLoginFile("file username", "file password"));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("file username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("file password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd2InFileThen1EmptyLine() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader = CredentialsReader.using(
        ClassicalCredentials.class, createApiLoginFile("file username", "file password", ""));
    credsReader.env = Map.of();

    final ImmutableCompleteMap<ClassicalCredentials, String> myAuth = credsReader.getCredentials();

    assertEquals("file username", myAuth.get(ClassicalCredentials.API_USERNAME));
    assertEquals("file password", myAuth.get(ClassicalCredentials.API_PASSWORD));
  }

  @Test
  public void test0SysPropsAnd0EnvAnd3InFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class,
            createApiLoginFile("file username", "file password", "supplementary"));
    credsReader.env = Map.of();

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @Test
  public void test0SysPropsAnd0EnvAnd3InFileWithEmptyLines() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class,
            createApiLoginFile("file username", "file password", "", "", "supplementary"));
    credsReader.env = Map.of();

    assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
  }

  @Test
  public void test0SysPropsAnd0EnvAndNoFile() throws Exception {
    final CredentialsReader<ClassicalCredentials> credsReader =
        CredentialsReader.using(ClassicalCredentials.class, getNonExistentFile());
    credsReader.env = Map.of();

    assertThrows(NoSuchElementException.class, () -> credsReader.getCredentials());
  }

  @AfterEach
  void closeJimFs() throws IOException {
    jimfs.close();
  }
}
