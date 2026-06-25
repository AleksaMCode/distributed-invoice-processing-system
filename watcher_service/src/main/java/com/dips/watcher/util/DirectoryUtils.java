package com.dips.watcher.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DirectoryUtils {
  private DirectoryUtils() {}

  public static void createDirectories(Path... directories) throws IOException {
    for (Path directory : directories) {
      Files.createDirectories(directory);
    }
  }
}
