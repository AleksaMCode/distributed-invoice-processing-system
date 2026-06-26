package com.dips.watcher.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileMoverTest {

  @TempDir Path tempDir;

  @Test
  void moveWithUuidSuffixMovesFileAndKeepsExtension() throws Exception {
    Path source = tempDir.resolve("invoice.xml");
    Path targetDir = tempDir.resolve("processed");
    Files.writeString(source, "<invoice/>", StandardCharsets.UTF_8);

    Path moved = FileMover.moveWithUuidSuffix(source, targetDir);

    assertFalse(Files.exists(source));
    assertTrue(Files.exists(moved));
    assertTrue(moved.getParent().equals(targetDir));
    assertTrue(moved.getFileName().toString().startsWith("invoice--"));
    assertTrue(moved.getFileName().toString().endsWith(".xml"));
  }

  @Test
  void moveWithUuidSuffixHandlesFilesWithoutExtension() throws Exception {
    Path source = tempDir.resolve("invoice");
    Path targetDir = tempDir.resolve("failed");
    Files.writeString(source, "<invoice/>", StandardCharsets.UTF_8);

    Path moved = FileMover.moveWithUuidSuffix(source, targetDir);

    assertFalse(Files.exists(source));
    assertTrue(Files.exists(moved));
    assertTrue(moved.getFileName().toString().startsWith("invoice--"));
    assertFalse(moved.getFileName().toString().endsWith("."));
  }
}
