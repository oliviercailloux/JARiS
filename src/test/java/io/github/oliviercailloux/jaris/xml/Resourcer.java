package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import io.github.oliviercailloux.jaris.io.PathUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Resourcer {
  public static ByteSource byteSource(String resourceName) {
    return PathUtils.fromResource(Resourcer.class, resourceName).asByteSource();
  }

  public static CharSource charSource(String resourceName) {
    return byteSource(resourceName).asCharSource(StandardCharsets.UTF_8);
  }

  public static CharSource charSource(URL source) {
    return Resources.asCharSource(source, StandardCharsets.UTF_8);
  }

  public static CharSource charSource(Path path) {
    return MoreFiles.asCharSource(path, StandardCharsets.UTF_8);
  }

  public static String titleTwoAuthorsOneLine() throws IOException {
    String textOneLine = charSource("Article ns/Title two authors.xml").read()
        .replaceAll("(?m)^\\h+", "").replaceAll("\n", "");
    assertTrue(textOneLine.contains("</k:Title><Authors><Author>Mr. Foo"), textOneLine);
    return textOneLine;
  }
}
