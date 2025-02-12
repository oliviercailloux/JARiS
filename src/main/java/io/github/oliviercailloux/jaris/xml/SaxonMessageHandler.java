package io.github.oliviercailloux.jaris.xml;

import java.util.function.Consumer;
import net.sf.saxon.s9api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SaxonMessageHandler implements Consumer<Message> {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(SaxonMessageHandler.class);

  private boolean hasBeenCalled = false;

  public static SaxonMessageHandler newInstance() {
    return new SaxonMessageHandler();
  }

  @Override
  public void accept(Message message) {
    LOGGER.debug("Received message while processing: {}.", message);
    hasBeenCalled = true;
  }

  public boolean hasBeenCalled() {
    return hasBeenCalled;
  }
}
