package com.dips.parser.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dips.validator.model.Invoice;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class InvoiceXmlParserTest {
  private final InvoiceXmlParser parser = new InvoiceXmlParser();

  @Test
  void parseMapsInvoiceFieldsFromXml() throws Exception {
    byte[] xml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <invoice>
          <id>INV-2026-001</id>
          <client>
            <name>DIPS</name>
            <ein>1234567890123</ein>
            <email>invoice@dips.com</email>
          </client>
          <date>2026-06-28</date>
          <currency>EUR</currency>
          <items>
            <item>
              <description>Web development</description>
              <quantity>10</quantity>
              <unitPrice>50.00</unitPrice>
            </item>
          </items>
        </invoice>
        """
            .getBytes(StandardCharsets.UTF_8);

    Invoice invoice = parser.parse(xml);

    assertEquals("INV-2026-001", invoice.id());
    assertNotNull(invoice.client());
    assertEquals("DIPS", invoice.client().name());
    assertEquals("1234567890123", invoice.client().ein());
    assertEquals("invoice@dips.com", invoice.client().email());
    assertEquals(LocalDate.of(2026, 6, 28), invoice.date());
    assertEquals("EUR", invoice.currency());
    assertEquals(1, invoice.items().size());
    assertEquals("Web development", invoice.items().get(0).description());
    assertEquals(10, invoice.items().get(0).quantity());
    assertEquals(new BigDecimal("50.00"), invoice.items().get(0).unitPrice());
  }

  @Test
  void extractInvoiceIdReturnsIdFromXml() throws Exception {
    byte[] xml = "<invoice><id>INV-42</id></invoice>".getBytes(StandardCharsets.UTF_8);

    String id = parser.extractInvoiceId(xml);

    assertEquals("INV-42", id);
  }

  @Test
  void parseThrowsOnMalformedXml() {
    byte[] invalidXml = "<invoice><id>INV-1</id>".getBytes(StandardCharsets.UTF_8);

    assertThrows(Exception.class, () -> parser.parse(invalidXml));
  }
}
