package com.dips.parser.config;

import io.github.cdimascio.dotenv.Dotenv;

public record ParserConfig(
    String parserHost,
    int parserPort,
    int parserWorkers,
    String validatorRmiHost,
    int validatorRmiPort,
    String validatorRmiBindName,
    String redisHost,
    int redisPort,
    String redisPassword,
    long invoiceIdTtlSeconds,
    String rabbitHost,
    int rabbitPort,
    String rabbitUser,
    String rabbitPassword,
    String rabbitVhost,
    String rabbitExchange,
    String rabbitExchangeType,
    String rabbitRoutingKeyValidated,
    String rabbitRoutingKeyRejected,
    String rabbitQueueValidated,
    String rabbitQueueRejected) {
  public static ParserConfig load() {
    Dotenv dotenv =
        Dotenv.configure().filename(".env").ignoreIfMissing().ignoreIfMalformed().load();

    return new ParserConfig(
        value(dotenv, "PARSER_HOST", "0.0.0.0"),
        intValue(dotenv, "PARSER_PORT", 5001),
        intValue(dotenv, "PARSER_WORKERS", 8),
        value(dotenv, "VALIDATOR_RMI_HOST", "127.0.0.1"),
        intValue(dotenv, "VALIDATOR_RMI_PORT", 1100),
        value(dotenv, "VALIDATOR_RMI_BIND_NAME", "ValidatorService"),
        value(dotenv, "REDIS_HOST", "127.0.0.1"),
        intValue(dotenv, "REDIS_PORT", 6379),
        value(dotenv, "REDIS_PASSWORD", ""),
        longValue(dotenv, "INVOICE_ID_TTL_SECONDS", 3600),
        value(dotenv, "RABBITMQ_HOST", "127.0.0.1"),
        intValue(dotenv, "RABBITMQ_PORT", 5672),
        value(dotenv, "RABBITMQ_USER", "guest"),
        value(dotenv, "RABBITMQ_PASSWORD", "guest"),
        value(dotenv, "RABBITMQ_VHOST", "/"),
        value(dotenv, "RABBITMQ_EXCHANGE", "dips.invoices"),
        value(dotenv, "RABBITMQ_EXCHANGE_TYPE", "direct"),
        value(dotenv, "RABBITMQ_ROUTING_KEY_VALIDATED", "validated"),
        value(dotenv, "RABBITMQ_ROUTING_KEY_REJECTED", "rejected"),
        value(dotenv, "RABBITMQ_QUEUE_VALIDATED", "invoices.validated"),
        value(dotenv, "RABBITMQ_QUEUE_REJECTED", "invoices.rejected"));
  }

  private static String value(Dotenv dotenv, String key, String fallback) {
    String fromEnv = System.getenv(key);
    if (fromEnv != null && !fromEnv.isBlank()) {
      return fromEnv.trim();
    }
    String fromDotenv = dotenv.get(key);
    if (fromDotenv != null && !fromDotenv.isBlank()) {
      return fromDotenv.trim();
    }
    return fallback;
  }

  private static int intValue(Dotenv dotenv, String key, int fallback) {
    return Integer.parseInt(value(dotenv, key, String.valueOf(fallback)));
  }

  private static long longValue(Dotenv dotenv, String key, long fallback) {
    return Long.parseLong(value(dotenv, key, String.valueOf(fallback)));
  }
}
