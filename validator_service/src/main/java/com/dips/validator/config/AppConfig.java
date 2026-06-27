package com.dips.validator.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.time.ZoneId;

public final class AppConfig {
  private final String rmiHost;
  private final int rmiPort;
  private final String rmiBindName;
  private final ZoneId validationZone;

  private AppConfig(String rmiHost, int rmiPort, String rmiBindName, ZoneId validationZone) {
    this.rmiHost = rmiHost;
    this.rmiPort = rmiPort;
    this.rmiBindName = rmiBindName;
    this.validationZone = validationZone;
  }

  public static AppConfig load() {
    Dotenv dotenv =
        Dotenv.configure().filename(".env").ignoreIfMissing().ignoreIfMalformed().load();

    String host = getValue(dotenv, "VALIDATOR_RMI_HOST", "127.0.0.1");
    int port = parseInt(getValue(dotenv, "VALIDATOR_RMI_PORT", "1100"), "VALIDATOR_RMI_PORT");
    String bindName = getValue(dotenv, "VALIDATOR_RMI_BIND_NAME", "ValidatorService");
    ZoneId zone = ZoneId.of(getValue(dotenv, "VALIDATOR_TIMEZONE", "Europe/Paris"));

    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("VALIDATOR_RMI_PORT must be in range 1-65535");
    }
    if (bindName.isBlank()) {
      throw new IllegalArgumentException("VALIDATOR_RMI_BIND_NAME must not be blank");
    }

    return new AppConfig(host, port, bindName, zone);
  }

  private static String getValue(Dotenv dotenv, String key, String fallback) {
    String env = System.getenv(key);
    if (env != null && !env.isBlank()) {
      return env.trim();
    }
    String dot = dotenv.get(key);
    if (dot != null && !dot.isBlank()) {
      return dot.trim();
    }
    return fallback;
  }

  private static int parseInt(String raw, String key) {
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(key + " must be an integer: " + raw, ex);
    }
  }

  public String rmiHost() {
    return rmiHost;
  }

  public int rmiPort() {
    return rmiPort;
  }

  public String rmiBindName() {
    return rmiBindName;
  }

  public ZoneId validationZone() {
    return validationZone;
  }
}
