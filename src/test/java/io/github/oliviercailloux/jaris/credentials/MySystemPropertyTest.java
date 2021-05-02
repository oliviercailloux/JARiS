package io.github.oliviercailloux.jaris.credentials;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

@ClearSystemProperty(key = "some property")
class MySystemPropertyTest {

  @Test
  @SetSystemProperty(key = "some property", value = "new value")
  void test() {
    assertEquals("new value", System.getProperty("some property"));
  }
}
