package io.github.oliviercailloux.jaris.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MoreCollectors;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class PathTests {

  @Test
  void testCloseDefaultFs() throws Exception {
    assertThrows(UnsupportedOperationException.class,
        () -> Path.of("ploum.txt").getFileSystem().close());
  }

  @Test
  public void testReadEmbeddedPath() throws Exception {
    String classFile = LoggerFactory.class.getSimpleName() + ".class";
    URI uri = LoggerFactory.class.getResource(classFile).toURI();
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
  }

  @Test
  public void testListClasspath() throws Exception {
    URL cat = getClass().getResource("/io/github/oliviercailloux/docbook/catalog.xml");
    assertNotNull(cat);
    URI uri = cat.toURI();
    assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
    try (FileSystem fs = FileSystems.newFileSystem(uri, ImmutableMap.of())) {
      Path path = Path.of(uri);
      assertTrue(Files.readString(path).contains("catalog"));
      ImmutableSet<Path> content =
          Files.list(path.getParent()).collect(ImmutableSet.toImmutableSet());
      ImmutableSet<URI> uris =
          content.stream().map(Path::toUri).collect(ImmutableSet.toImmutableSet());
      assertTrue(uris.stream().anyMatch(u -> u.toString().endsWith("catalog.xml")));
      URI foundCatalog = uris.stream().filter(u -> u.toString().endsWith("catalog.xml"))
          .collect(MoreCollectors.onlyElement());
      assertEquals(foundCatalog.toString().replace("jar:file:///", "jar:file:/"), uri.toString());
    }
    /* Seems to depend on timing issues, sometimes not thrown when running many tests in a row. */
    // assertThrows(FileSystemNotFoundException.class, () -> Path.of(uri));
  }
}
