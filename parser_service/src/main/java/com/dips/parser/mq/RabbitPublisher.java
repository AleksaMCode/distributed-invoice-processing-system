package com.dips.parser.mq;

import com.dips.parser.config.ParserConfig;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class RabbitPublisher implements AutoCloseable {
  private final ParserConfig config;
  private final ObjectMapper objectMapper;
  private final Connection connection;
  private final Channel channel;

  public RabbitPublisher(ParserConfig config) throws Exception {
    this.config = config;
    this.objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(config.rabbitHost());
    factory.setPort(config.rabbitPort());
    factory.setUsername(config.rabbitUser());
    factory.setPassword(config.rabbitPassword());
    factory.setVirtualHost(config.rabbitVhost());

    this.connection = factory.newConnection();
    this.channel = connection.createChannel();

    channel.exchangeDeclare(config.rabbitExchange(), config.rabbitExchangeType(), true);
    channel.queueDeclare(config.rabbitQueueValidated(), true, false, false, null);
    channel.queueDeclare(config.rabbitQueueRejected(), true, false, false, null);
    channel.queueBind(
        config.rabbitQueueValidated(), config.rabbitExchange(), config.rabbitRoutingKeyValidated());
    channel.queueBind(
        config.rabbitQueueRejected(), config.rabbitExchange(), config.rabbitRoutingKeyRejected());
  }

  public void publishValidated(Invoice invoice, ValidationResult result, String fileName)
      throws Exception {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("status", "validated");
    payload.put("invoice", invoice);
    payload.put("validatorTimestamp", result.validatorTimestamp());
    payload.put("sourceFile", fileName);
    payload.put("publishedAt", OffsetDateTime.now().toString());
    publish(config.rabbitRoutingKeyValidated(), payload);
  }

  public void publishRejected(
      Invoice invoice, String invoiceId, String fileName, String reason, Object details)
      throws Exception {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("status", "rejected");
    payload.put("invoiceId", invoiceId);
    payload.put("sourceFile", fileName);
    payload.put("reason", reason);
    payload.put("details", details);
    payload.put("publishedAt", OffsetDateTime.now().toString());
    if (invoice != null) {
      payload.put("invoice", invoice);
    }
    publish(config.rabbitRoutingKeyRejected(), payload);
  }

  private void publish(String routingKey, Map<String, Object> payload) throws Exception {
    byte[] body = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
    channel.basicPublish(config.rabbitExchange(), routingKey, null, body);
  }

  @Override
  public void close() throws Exception {
    channel.close();
    connection.close();
  }
}
