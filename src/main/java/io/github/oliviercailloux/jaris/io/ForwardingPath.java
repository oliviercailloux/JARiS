package io.github.oliviercailloux.jaris.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class ForwardingPath implements Path {

  protected abstract Path delegate();

  @Override
  public FileSystem getFileSystem() {
    return delegate().getFileSystem();
  }

  @Override
  public boolean isAbsolute() {
    return delegate().isAbsolute();
  }

  @Override
  public Path toAbsolutePath() {
    return delegate().toAbsolutePath();
  }

  @Override
  public Path getRoot() {
    return delegate().getRoot();
  }

  @Override
  public int getNameCount() {
    return delegate().getNameCount();
  }

  @Override
  public Path getName(int index) {
    return delegate().getName(index);
  }

  @Override
  public Path subpath(int beginIndex, int endIndex) {
    return delegate().subpath(beginIndex, endIndex);
  }

  @Override
  public Path getFileName() {
    return delegate().getFileName();
  }

  @Override
  public Path getParent() {
    return delegate().getParent();
  }

  @Override
  public boolean startsWith(Path other) {
    return delegate().startsWith(other);
  }

  @Override
  public boolean startsWith(String other) {
    return delegate().startsWith(other);
  }

  @Override
  public boolean endsWith(Path other) {
    return delegate().endsWith(other);
  }

  @Override
  public boolean endsWith(String other) {
    return delegate().endsWith(other);
  }

  @Override
  public Path normalize() {
    return delegate().normalize();
  }

  @Override
  public Path resolve(Path other) {
    return delegate().resolve(other);
  }

  @Override
  public Path resolve(String other) {
    return delegate().resolve(other);
  }

  @Override
  public Path relativize(Path other) {
    return delegate().relativize(other);
  }

  @Override
  public URI toUri() {
    return delegate().toUri();
  }

  @Override
  public Path toRealPath(LinkOption... options)
      throws IOException {
    return delegate().toRealPath(options);
  }

  @Override
  public int compareTo(Path other) {
    return delegate().compareTo(other);
  }

  @Override
  public boolean equals(Object o2) {
    return delegate().equals(o2);
  }

  @Override
  public int hashCode() {
    return delegate().hashCode();
  }

  @Override
  public String toString() {
    return delegate().toString();
  }

  @Override
  public Iterator<Path> iterator() {
    return delegate().iterator();
  }

  @Override
  public Spliterator<Path> spliterator() {
    return delegate().spliterator();
  }

  @Override
  public void forEach(Consumer<? super Path> action) {
    delegate().forEach(action);
  }

  @Override
  public Path resolveSibling(Path other) {
    return delegate().resolveSibling(other);
  }

  @Override
  public Path resolveSibling(String other) {
    return delegate().resolveSibling(other);
  }

  @Override
  public File toFile() {
    return delegate().toFile();
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers)
      throws IOException {
    return delegate().register(watcher, events, modifiers);
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
    return delegate().register(watcher, events);
  }
}
