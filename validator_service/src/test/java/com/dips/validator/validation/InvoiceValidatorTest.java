package com.dips.validator.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dips.validator.model.Client;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.Item;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceValidatorTest {
  private static final ZoneId ZONE = ZoneId.of("Europe/Paris");

  private InvoiceValidator validator;

  @BeforeEach
  void setUp() {
    validator = new InvoiceValidator(ZONE);
  }

  @Test
  void validateReturnsNoErrorsForValidInvoice() {
    List<String> errors = validator.validate(validInvoice());

    assertTrue(errors.isEmpty());
  }

  @Test
  void validateReturnsInvoiceNullWhenInvoiceMissing() {
    List<String> errors = validator.validate(null);

    assertTrue(errors.contains("INVOICE_NULL"));
  }

  @Test
  void validateReturnsClientEinInvalidForWrongEin() {
    Invoice invoice = withClient(new Client("DIPS", "12345", "invoice@dips.com"));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("CLIENT_EIN_INVALID"));
  }

  @Test
  void validateReturnsClientEmailInvalidForWrongEmail() {
    Invoice invoice = withClient(new Client("DIPS", "1234567890123", "invalid-email"));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("CLIENT_EMAIL_INVALID"));
  }

  @Test
  void validateReturnsDateInFutureForFutureDate() {
    Invoice invoice = withDate(LocalDate.now(ZONE).plusDays(1));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_DATE_IN_FUTURE"));
  }

  @Test
  void validateReturnsDateTooOldForDateBefore365Days() {
    Invoice invoice = withDate(LocalDate.now(ZONE).minusDays(366));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_DATE_TOO_OLD"));
  }

  @Test
  void validateReturnsCurrencyInvalidForUnsupportedCurrency() {
    Invoice invoice = withCurrency("JPY");

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_CURRENCY_INVALID"));
  }

  @Test
  void validateReturnsItemsEmptyForEmptyItems() {
    Invoice invoice = withItems(List.of());

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_ITEMS_EMPTY"));
  }

  @Test
  void validateReturnsQuantityInvalidForNonPositiveQuantity() {
    Invoice invoice = withItems(List.of(new Item("Service", 0, new BigDecimal("10.00"))));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_ITEM_QUANTITY_INVALID_AT_0"));
  }

  @Test
  void validateReturnsUnitPriceInvalidForNonPositiveUnitPrice() {
    Invoice invoice = withItems(List.of(new Item("Service", 1, BigDecimal.ZERO)));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_ITEM_UNIT_PRICE_INVALID_AT_0"));
  }

  @Test
  void validateReturnsTotalTooLargeForTotalAtOrAboveLimit() {
    Invoice invoice = withItems(List.of(new Item("Big project", 1, new BigDecimal("1000000.00"))));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_TOTAL_TOO_LARGE"));
  }

  @Test
  void validateReturnsTotalInvalidWhenNoValidLineItemsContribute() {
    Invoice invoice = withItems(List.of(new Item("Broken line", 0, new BigDecimal("100.00"))));

    List<String> errors = validator.validate(invoice);

    assertTrue(errors.contains("INVOICE_TOTAL_INVALID"));
  }

  @Test
  void validateAcceptsAllAllowedCurrencies() {
    for (String currency : List.of("BAM", "EUR", "USD", "CHF", "GBP")) {
      List<String> errors = validator.validate(withCurrency(currency));
      assertFalse(errors.contains("INVOICE_CURRENCY_INVALID"));
    }
  }

  private Invoice validInvoice() {
    return new Invoice(
        "INV-2026-001",
        new Client("DIPS", "1234567890123", "invoice@dips.com"),
        LocalDate.now(ZONE).minusDays(1),
        "EUR",
        List.of(
            new Item("Web development", 10, new BigDecimal("50.00")),
            new Item("Hosting", 1, new BigDecimal("120.00"))));
  }

  private Invoice withClient(Client client) {
    Invoice base = validInvoice();
    return new Invoice(base.id(), client, base.date(), base.currency(), base.items());
  }

  private Invoice withDate(LocalDate date) {
    Invoice base = validInvoice();
    return new Invoice(base.id(), base.client(), date, base.currency(), base.items());
  }

  private Invoice withCurrency(String currency) {
    Invoice base = validInvoice();
    return new Invoice(base.id(), base.client(), base.date(), currency, base.items());
  }

  private Invoice withItems(List<Item> items) {
    Invoice base = validInvoice();
    return new Invoice(base.id(), base.client(), base.date(), base.currency(), items);
  }
}
