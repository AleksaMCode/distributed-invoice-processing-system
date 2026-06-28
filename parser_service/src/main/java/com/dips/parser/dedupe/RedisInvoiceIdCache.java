package com.dips.parser.dedupe;

import com.dips.parser.config.ParserConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.SetParams;

public class RedisInvoiceIdCache implements InvoiceIdCache {
  private final JedisPooled jedis;
  private final long ttlSeconds;

  public RedisInvoiceIdCache(ParserConfig config) {
    this.ttlSeconds = config.invoiceIdTtlSeconds();
    if (config.redisPassword() == null || config.redisPassword().isBlank()) {
      this.jedis = new JedisPooled(config.redisHost(), config.redisPort());
    } else {
      this.jedis =
          new JedisPooled(config.redisHost(), config.redisPort(), null, config.redisPassword());
    }
  }

  @Override
  public boolean reserveIfNew(String invoiceId) {
    String key = "invoice:id:" + invoiceId;
    String result = jedis.set(key, "1", SetParams.setParams().nx().ex(ttlSeconds));
    return "OK".equals(result);
  }

  @Override
  public void close() {
    jedis.close();
  }
}
