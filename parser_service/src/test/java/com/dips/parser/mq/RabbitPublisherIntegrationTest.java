package com.dips.parser.mq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dips.parser.config.ParserConfig;
import com.dips.validator.model.Client;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.Item;
import com.dips.validator.model.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class RabbitPublisherIntegrationTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Container
  static final RabbitMQContainer RABBIT =
      new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management"));

  @Test
  void publishValidatedWritesMessageToValidatedQueue() throws Exception {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    ParserConfig config = config(suffix);

    Invoice invoice =
        new Invoice(
            "INV-RABBIT-001",
            new Client("DIPS", "1234567890123", "invoice@dips.com"),
            LocalDate.of(2026, 6, 28),
            "EUR",
            List.of(new Item("Web development", 2, new BigDecimal("100.00"))));
    ValidationResult validationResult =
        new ValidationResult(true, List.of(), "2026-06-28T10:15:30Z");

    try (RabbitPublisher publisher = new RabbitPublisher(config)) {
      publisher.publishValidated(invoice, validationResult, "invoice-a.xml");
    }

    JsonNode payload = consumeSingle(config.rabbitQueueValidated());
    assertEquals("validated", payload.get("status").asText());
    assertEquals("invoice-a.xml", payload.get("sourceFile").asText());
    assertEquals("INV-RABBIT-001", payload.get("invoice").get("id").asText());
    assertEquals("EUR", payload.get("invoice").get("currency").asText());
    assertEquals("2026-06-28", payload.get("invoice").get("date").asText());
  }

  @Test
  void publishRejectedWritesMessageToRejectedQueue() throws Exception {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    ParserConfig config = config(suffix);

    try (RabbitPublisher publisher = new RabbitPublisher(config)) {
      publisher.publishRejected(
          null,
          "INV-RABBIT-REJECT-1",
          "invoice-b.xml",
          "DUPLICATE_INVOICE_ID",
          "Invoice id already exists");
    }

    JsonNode payload = consumeSingle(config.rabbitQueueRejected());
    assertEquals("rejected", payload.get("status").asText());
    assertEquals("INV-RABBIT-REJECT-1", payload.get("invoiceId").asText());
    assertEquals("invoice-b.xml", payload.get("sourceFile").asText());
    assertEquals("DUPLICATE_INVOICE_ID", payload.get("reason").asText());
    assertTrue(payload.has("publishedAt"));
  }

  private JsonNode consumeSingle(String queueName) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBIT.getHost());
    factory.setPort(RABBIT.getAmqpPort());
    factory.setUsername(RABBIT.getAdminUsername());
    factory.setPassword(RABBIT.getAdminPassword());
    factory.setVirtualHost("/");

    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      GetResponse response = channel.basicGet(queueName, true);
      assertNotNull(response, "Expected one message in queue " + queueName);
      byte[] body = response.getBody();
      return OBJECT_MAPPER.readTree(new String(body, StandardCharsets.UTF_8));
    }
  }

  private ParserConfig config(String suffix) {
    return new ParserConfig(
        "0.0.0.0",
        5001,
        1,
        "127.0.0.1",
        1099,
        "ValidatorService",
        "127.0.0.1",
        6379,
        "",
        3600,
        RABBIT.getHost(),
        RABBIT.getAmqpPort(),
        RABBIT.getAdminUsername(),
        RABBIT.getAdminPassword(),
        "/",
        "dips.invoices." + suffix,
        "direct",
        "validated",
        "rejected",
        "invoices.validated." + suffix,
        "invoices.rejected." + suffix);
  }
}
