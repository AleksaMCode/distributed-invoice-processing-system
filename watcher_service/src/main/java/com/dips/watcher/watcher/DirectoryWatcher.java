package com.dips.watcher.watcher;

import com.dips.watcher.service.PathRouter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);

  private final Path inboxDir;
  private final PathRouter pathRouter;
  private final int stabilityChecks;
  private final long stabilityDelayMs;
  private final long watchPollTimeoutMs;

  public DirectoryWatcher(
      Path inboxDir,
      PathRouter pathRouter,
      int stabilityChecks,
      long stabilityDelayMs,
      long watchPollTimeoutMs) {
    this.inboxDir = inboxDir;
    this.pathRouter = pathRouter;
    this.stabilityChecks = stabilityChecks;
    this.stabilityDelayMs = stabilityDelayMs;
    this.watchPollTimeoutMs = watchPollTimeoutMs;
  }

  @Override
  public void run() {
    try {
      enqueueExistingXmlFiles();
      watchLoop();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("Directory watcher stopped due to error", e);
    }
  }

  private void watchLoop() throws IOException, InterruptedException {
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      inboxDir.register(
          watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.OVERFLOW);

      logger.info("Watching directory: {}", inboxDir);
      while (!Thread.currentThread().isInterrupted()) {
        WatchKey key = watchService.poll(watchPollTimeoutMs, TimeUnit.MILLISECONDS);
        if (key == null) {
          continue;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
          if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
            logger.warn("WatchService overflow detected, rescanning inbox");
            enqueueExistingXmlFiles();
            continue;
          }

          if (event.kind() != StandardWatchEventKinds.ENTRY_CREATE) {
            continue;
          }

          @SuppressWarnings("unchecked")
          WatchEvent<Path> createEvent = (WatchEvent<Path>) event;
          Path candidate = inboxDir.resolve(createEvent.context()).toAbsolutePath().normalize();
          enqueueIfEligible(candidate);
        }

        boolean valid = key.reset();
        if (!valid) {
          logger.error("WatchKey became invalid; stopping watcher");
          return;
        }
      }
    }
  }

  private void enqueueExistingXmlFiles() throws InterruptedException, IOException {
    if (!Files.exists(inboxDir)) {
      return;
    }

    try (var stream = Files.list(inboxDir)) {
      stream
          .filter(Files::isRegularFile)
          .filter(this::isXmlFile)
          .sorted()
          .forEach(
              path -> {
                try {
                  enqueueIfEligible(path);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              });
    }
  }

  private void enqueueIfEligible(Path file) throws InterruptedException {
    if (!isXmlFile(file)) {
      return;
    }
    boolean stable = waitUntilStable(file);
    if (!stable) {
      logger.warn("File never stabilized, skipping {}", file.getFileName());
      return;
    }
    pathRouter.submit(file);
    logger.info("Queued {}", file.getFileName());
  }

  private boolean isXmlFile(Path path) {
    String filename = path.getFileName().toString().toLowerCase(Locale.ROOT);
    return filename.endsWith(".xml");
  }

  private boolean waitUntilStable(Path path) throws InterruptedException {
    long previousSize = -1L;
    int consecutiveStableChecks = 0;

    while (consecutiveStableChecks < stabilityChecks && !Thread.currentThread().isInterrupted()) {
      if (!Files.exists(path) || !Files.isRegularFile(path)) {
        return false;
      }

      long currentSize;
      try {
        currentSize = Files.size(path);
      } catch (IOException ex) {
        return false;
      }

      if (currentSize == previousSize) {
        consecutiveStableChecks++;
      } else {
        consecutiveStableChecks = 1;
        previousSize = currentSize;
      }

      Thread.sleep(stabilityDelayMs);
    }
    return !Thread.currentThread().isInterrupted();
  }
}
