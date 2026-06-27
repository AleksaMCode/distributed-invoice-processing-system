package com.dips.validator.rmi;

import com.dips.validator.model.Invoice;
import com.dips.validator.model.ValidationResult;
import com.dips.validator.validation.InvoiceValidator;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorServiceImpl extends UnicastRemoteObject implements ValidatorService {
  private static final Logger logger = LoggerFactory.getLogger(ValidatorServiceImpl.class);

  private final InvoiceValidator invoiceValidator;

  public ValidatorServiceImpl(InvoiceValidator invoiceValidator) throws RemoteException {
    super();
    this.invoiceValidator = invoiceValidator;
  }

  @Override
  public ValidationResult validate(Invoice invoice) throws RemoteException {
    List<String> errors = invoiceValidator.validate(invoice);
    boolean valid = errors.isEmpty();
    String timestamp = OffsetDateTime.now().toString();

    if (valid) {
      logger.info("Invoice {} validated successfully", invoice != null ? invoice.id() : "UNKNOWN");
    } else {
      logger.warn("Invoice validation failed with {} errors: {}", errors.size(), errors);
    }

    return new ValidationResult(valid, errors, timestamp);
  }
}
