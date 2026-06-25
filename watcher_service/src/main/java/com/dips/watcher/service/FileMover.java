package com.dips.watcher.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class FileMover {
  private FileMover() {}

  public static Path moveWithUuidSuffix(Path source, Path targetDir) throws IOException {
    Files.createDirectories(targetDir);
    String originalName = source.getFileName().toString();
    String uuid = UUID.randomUUID().toString();

    int dot = originalName.lastIndexOf('.');
    String base = dot > 0 ? originalName.substring(0, dot) : originalName;
    String extension = dot > 0 ? originalName.substring(dot) : "";
    String newName = base + "--" + uuid + extension;

    Path target = targetDir.resolve(newName);
    return Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
  }
}
