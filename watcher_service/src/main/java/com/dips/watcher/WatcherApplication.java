package com.dips.watcher;

import com.dips.watcher.config.AppConfig;
import com.dips.watcher.net.ParserClient;
import com.dips.watcher.service.InvoiceProcessor;
import com.dips.watcher.service.PathRouter;
import com.dips.watcher.util.DirectoryUtils;
import com.dips.watcher.watcher.DirectoryWatcher;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatcherApplication {
  private static final Logger logger = LoggerFactory.getLogger(WatcherApplication.class);

  public static void main(String[] args) throws Exception {
    AppConfig config = AppConfig.load();
    DirectoryUtils.createDirectories(config.inboxDir(), config.processedDir(), config.failedDir());

    BlockingQueue<Path> queue = new ArrayBlockingQueue<>(config.queueCapacity());
    PathRouter pathRouter = new PathRouter(queue);

    ParserClient parserClient =
        new ParserClient(
            config.parserHost(),
            config.parserPort(),
            config.socketConnectTimeoutMs(),
            config.socketReadTimeoutMs());

    Thread processorThread =
        new Thread(
            new InvoiceProcessor(
                config.processedDir(), config.failedDir(), queue, parserClient, pathRouter),
            "invoice-processor");

    Thread watcherThread =
        new Thread(
            new DirectoryWatcher(
                config.inboxDir(),
                pathRouter,
                config.fileStabilityChecks(),
                config.fileStabilityDelayMs(),
                config.watchPollTimeoutMs()),
            "directory-watcher");

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("Shutdown requested, stopping watcher service...");
                  watcherThread.interrupt();
                  processorThread.interrupt();
                },
                "watcher-shutdown-hook"));

    logger.info(
        "Watcher starting with inbox={}, processed={}, failed={}, parser={}:{} queueCapacity={}",
        config.inboxDir(),
        config.processedDir(),
        config.failedDir(),
        config.parserHost(),
        config.parserPort(),
        config.queueCapacity());

    processorThread.start();
    watcherThread.start();

    watcherThread.join();
    processorThread.join();
  }
}
