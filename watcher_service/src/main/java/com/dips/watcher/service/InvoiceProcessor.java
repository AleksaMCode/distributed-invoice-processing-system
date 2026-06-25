package com.dips.watcher.service;

import com.dips.watcher.net.AckResponse;
import com.dips.watcher.net.ParserClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceProcessor implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(InvoiceProcessor.class);

  private final Path processedDir;
  private final Path failedDir;
  private final java.util.concurrent.BlockingQueue<Path> queue;
  private final ParserClient parserClient;
  private final PathRouter pathRouter;

  public InvoiceProcessor(
      Path processedDir,
      Path failedDir,
      java.util.concurrent.BlockingQueue<Path> queue,
      ParserClient parserClient,
      PathRouter pathRouter) {
    this.processedDir = processedDir;
    this.failedDir = failedDir;
    this.queue = queue;
    this.parserClient = parserClient;
    this.pathRouter = pathRouter;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      Path file = null;
      try {
        file = queue.take();
        processSingle(file);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        if (file != null) {
          logger.error("Unexpected processing error for file {}", file, e);
          moveSafely(file, failedDir, "unexpected processor error");
        }
      } finally {
        if (file != null) {
          pathRouter.complete(file);
        }
      }
    }
  }

  private void processSingle(Path file) {
    if (!Files.exists(file)) {
      logger.warn("Queued file no longer exists, skipping: {}", file);
      return;
    }

    try {
      byte[] xmlBytes = Files.readAllBytes(file);
      String sourceFileName = file.getFileName().toString();
      byte[] frame = FrameBuilder.buildFrame(xmlBytes, sourceFileName);
      AckResponse ackResponse = parserClient.send(frame);

      if (ackResponse.acknowledged() && sourceFileName.equals(ackResponse.fileName())) {
        Path moved = FileMover.moveWithUuidSuffix(file, processedDir);
        logger.info(
            "ACK received for {}, echoed='{}', moved to {}",
            file.getFileName(),
            ackResponse.fileName(),
            moved);
      } else {
        logger.warn(
            "ACK invalid/missing/mismatched for {}, echoed='{}', raw='{}'; moving to failed",
            file.getFileName(),
            ackResponse.fileName(),
            ackResponse.raw());
        moveSafely(file, failedDir, "invalid or missing ACK");
      }
    } catch (IOException ioException) {
      logger.warn(
          "No ACK / socket issue for {}, moving to failed: {}",
          file.getFileName(),
          ioException.getMessage());
      moveSafely(file, failedDir, "socket or I/O issue");
    }
  }

  private void moveSafely(Path source, Path targetDir, String reason) {
    try {
      if (!Files.exists(source)) {
        return;
      }
      Path moved = FileMover.moveWithUuidSuffix(source, targetDir);
      logger.info("Moved {} to {} due to {}", source.getFileName(), moved, reason);
    } catch (Exception moveEx) {
      logger.error("Failed to move {} to {}", source, targetDir, moveEx);
    }
  }
}
