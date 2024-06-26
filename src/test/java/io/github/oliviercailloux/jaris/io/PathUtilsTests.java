package io.github.oliviercailloux.jaris.io;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.jimfs.Jimfs;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtilsTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(PathUtilsTests.class);

  @Test
  public void testCopyRecursively() throws Exception {
    try (FileSystem src = Jimfs.newFileSystem()) {
      Path root = Files.createDirectories(src.getPath("root/"));
      Files.createFile(root.resolve("file"));
      Path subdir = Files.createDirectories(root.resolve("subdir/"));
      Files.createFile(subdir.resolve("subfile"));
      
      Path copied = src.getPath("copied/");
      PathUtils.copyRecursively(root, copied);
      assertTrue(Files.exists(copied.resolve("file")));
      assertTrue(Files.exists(copied.resolve("subdir/subfile")));
    }
  }
}
