package com.dips.validator.validation;

import com.dips.validator.model.Client;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.Item;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.validator.routines.EmailValidator;

public class InvoiceValidator {
  private static final Set<String> ALLOWED_CURRENCIES = Set.of("BAM", "EUR", "USD", "CHF", "GBP");
  private static final BigDecimal MAX_TOTAL = new BigDecimal("1000000");

  private final ZoneId validationZone;

  public InvoiceValidator(ZoneId validationZone) {
    this.validationZone = validationZone;
  }

  public List<String> validate(Invoice invoice) {
    List<String> errors = new ArrayList<>();

    if (invoice == null) {
      errors.add("INVOICE_NULL");
      return errors;
    }

    validateClient(invoice.client(), errors);
    validateDate(invoice.date(), errors);
    validateCurrency(invoice.currency(), errors);
    validateItems(invoice.items(), errors);
    validateTotal(invoice.items(), errors);

    return errors;
  }

  private void validateClient(Client client, List<String> errors) {
    if (client == null) {
      errors.add("CLIENT_MISSING");
      return;
    }

    String ein = normalize(client.ein());
    if (ein == null || !ein.matches("\\d{13}")) {
      errors.add("CLIENT_EIN_INVALID");
    }

    String email = normalize(client.email());
    if (email == null || email.length() > 254 || !EmailValidator.getInstance().isValid(email)) {
      errors.add("CLIENT_EMAIL_INVALID");
    }
  }

  private void validateDate(LocalDate date, List<String> errors) {
    if (date == null) {
      errors.add("INVOICE_DATE_MISSING");
      return;
    }

    LocalDate today = LocalDate.now(validationZone);
    if (date.isAfter(today)) {
      errors.add("INVOICE_DATE_IN_FUTURE");
    }
    if (date.isBefore(today.minusDays(365))) {
      errors.add("INVOICE_DATE_TOO_OLD");
    }
  }

  private void validateCurrency(String currency, List<String> errors) {
    String normalized = normalize(currency);
    if (normalized == null || !ALLOWED_CURRENCIES.contains(normalized.toUpperCase(Locale.ROOT))) {
      errors.add("INVOICE_CURRENCY_INVALID");
    }
  }

  private void validateItems(List<Item> items, List<String> errors) {
    if (items == null || items.isEmpty()) {
      errors.add("INVOICE_ITEMS_EMPTY");
      return;
    }

    for (int index = 0; index < items.size(); index++) {
      Item item = items.get(index);
      if (item == null) {
        errors.add("INVOICE_ITEM_NULL_AT_" + index);
        continue;
      }
      if (item.quantity() == null || item.quantity() <= 0) {
        errors.add("INVOICE_ITEM_QUANTITY_INVALID_AT_" + index);
      }
      if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
        errors.add("INVOICE_ITEM_UNIT_PRICE_INVALID_AT_" + index);
      }
    }
  }

  private void validateTotal(List<Item> items, List<String> errors) {
    if (items == null || items.isEmpty()) {
      return;
    }

    BigDecimal total = BigDecimal.ZERO;
    for (Item item : items) {
      if (item == null || item.quantity() == null || item.unitPrice() == null) {
        continue;
      }
      if (item.quantity() <= 0 || item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      total = total.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
    }

    if (total.compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("INVOICE_TOTAL_INVALID");
    }
    if (total.compareTo(MAX_TOTAL) >= 0) {
      errors.add("INVOICE_TOTAL_TOO_LARGE");
    }
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
