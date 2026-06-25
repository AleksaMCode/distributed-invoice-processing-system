package com.dips.watcher.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {
  private final Path inboxDir;
  private final Path processedDir;
  private final Path failedDir;
  private final String parserHost;
  private final int parserPort;
  private final int queueCapacity;
  private final int socketConnectTimeoutMs;
  private final int socketReadTimeoutMs;
  private final int fileStabilityChecks;
  private final long fileStabilityDelayMs;
  private final long watchPollTimeoutMs;

  private AppConfig(
      Path inboxDir,
      Path processedDir,
      Path failedDir,
      String parserHost,
      int parserPort,
      int queueCapacity,
      int socketConnectTimeoutMs,
      int socketReadTimeoutMs,
      int fileStabilityChecks,
      long fileStabilityDelayMs,
      long watchPollTimeoutMs) {
    this.inboxDir = inboxDir;
    this.processedDir = processedDir;
    this.failedDir = failedDir;
    this.parserHost = parserHost;
    this.parserPort = parserPort;
    this.queueCapacity = queueCapacity;
    this.socketConnectTimeoutMs = socketConnectTimeoutMs;
    this.socketReadTimeoutMs = socketReadTimeoutMs;
    this.fileStabilityChecks = fileStabilityChecks;
    this.fileStabilityDelayMs = fileStabilityDelayMs;
    this.watchPollTimeoutMs = watchPollTimeoutMs;
  }

  public static AppConfig load() {
    Dotenv dotenv =
        Dotenv.configure().filename(".env").ignoreIfMalformed().ignoreIfMissing().load();

    Path inbox = toPath(getConfigValue(dotenv, "INBOX_DIR", "./invoices/inbox"));
    Path processed = toPath(getConfigValue(dotenv, "PROCESSED_DIR", "./invoices/processed"));
    Path failed = toPath(getConfigValue(dotenv, "FAILED_DIR", "./invoices/failed"));

    String parserHost = getConfigValue(dotenv, "PARSER_HOST", "localhost");
    int parserPort = getInt(getConfigValue(dotenv, "PARSER_PORT", "5001"), "PARSER_PORT");
    int queueCapacity = getInt(getConfigValue(dotenv, "QUEUE_CAPACITY", "1000"), "QUEUE_CAPACITY");
    int connectTimeoutMs =
        getInt(
            getConfigValue(dotenv, "SOCKET_CONNECT_TIMEOUT_MS", "3000"),
            "SOCKET_CONNECT_TIMEOUT_MS");
    int readTimeoutMs =
        getInt(getConfigValue(dotenv, "SOCKET_READ_TIMEOUT_MS", "5000"), "SOCKET_READ_TIMEOUT_MS");
    int stabilityChecks =
        getInt(getConfigValue(dotenv, "FILE_STABILITY_CHECKS", "2"), "FILE_STABILITY_CHECKS");
    long stabilityDelayMs =
        getLong(
            getConfigValue(dotenv, "FILE_STABILITY_DELAY_MS", "500"), "FILE_STABILITY_DELAY_MS");
    long watchPollTimeoutMs =
        getLong(getConfigValue(dotenv, "WATCH_POLL_TIMEOUT_MS", "500"), "WATCH_POLL_TIMEOUT_MS");

    if (queueCapacity < 1) {
      throw new IllegalArgumentException("QUEUE_CAPACITY must be >= 1");
    }
    if (stabilityChecks < 1) {
      throw new IllegalArgumentException("FILE_STABILITY_CHECKS must be >= 1");
    }

    return new AppConfig(
        inbox,
        processed,
        failed,
        parserHost,
        parserPort,
        queueCapacity,
        connectTimeoutMs,
        readTimeoutMs,
        stabilityChecks,
        stabilityDelayMs,
        watchPollTimeoutMs);
  }

  private static Path toPath(String raw) {
    return Paths.get(raw).toAbsolutePath().normalize();
  }

  private static String getConfigValue(Dotenv dotenv, String key, String fallback) {
    String env = System.getenv(key);
    if (env != null && !env.isBlank()) {
      return env;
    }
    String dot = dotenv.get(key);
    if (dot != null && !dot.isBlank()) {
      return dot;
    }
    return fallback;
  }

  private static int getInt(String value, String key) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(key + " must be an integer, but was: " + value, ex);
    }
  }

  private static long getLong(String value, String key) {
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(key + " must be a long, but was: " + value, ex);
    }
  }

  public Path inboxDir() {
    return inboxDir;
  }

  public Path processedDir() {
    return processedDir;
  }

  public Path failedDir() {
    return failedDir;
  }

  public String parserHost() {
    return parserHost;
  }

  public int parserPort() {
    return parserPort;
  }

  public int queueCapacity() {
    return queueCapacity;
  }

  public int socketConnectTimeoutMs() {
    return socketConnectTimeoutMs;
  }

  public int socketReadTimeoutMs() {
    return socketReadTimeoutMs;
  }

  public int fileStabilityChecks() {
    return fileStabilityChecks;
  }

  public long fileStabilityDelayMs() {
    return fileStabilityDelayMs;
  }

  public long watchPollTimeoutMs() {
    return watchPollTimeoutMs;
  }
}
