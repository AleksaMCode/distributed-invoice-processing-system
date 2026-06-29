package com.dips.parser.dedupe;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dips.parser.config.ParserConfig;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class RedisInvoiceIdCacheIntegrationTest {

  @Container
  static final GenericContainer<?> REDIS =
      new GenericContainer<>(DockerImageName.parse("redis:8.8-alpine")).withExposedPorts(6379);

  @Test
  void reserveIfNewReturnsTrueThenFalseForSameInvoiceId() {
    ParserConfig config = config(3600);

    try (RedisInvoiceIdCache cache = new RedisInvoiceIdCache(config)) {
      assertTrue(cache.reserveIfNew("INV-REDIS-001"));
      assertFalse(cache.reserveIfNew("INV-REDIS-001"));
    }
  }

  @Test
  void reserveIfNewAllowsReinsertAfterTtlExpires() throws Exception {
    ParserConfig config = config(1);

    try (RedisInvoiceIdCache cache = new RedisInvoiceIdCache(config)) {
      assertTrue(cache.reserveIfNew("INV-REDIS-TTL-001"));
      assertFalse(cache.reserveIfNew("INV-REDIS-TTL-001"));
      Thread.sleep(1200);
      assertTrue(cache.reserveIfNew("INV-REDIS-TTL-001"));
    }
  }

  private ParserConfig config(long ttlSeconds) {
    return new ParserConfig(
        "0.0.0.0",
        5001,
        1,
        "127.0.0.1",
        1099,
        "ValidatorService",
        REDIS.getHost(),
        REDIS.getMappedPort(6379),
        "",
        ttlSeconds,
        "127.0.0.1",
        5672,
        "guest",
        "guest",
        "/",
        "dips.invoices",
        "direct",
        "validated",
        "rejected",
        "invoices.validated",
        "invoices.rejected");
  }
}
