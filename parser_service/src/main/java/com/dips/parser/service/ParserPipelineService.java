package com.dips.parser.service;

import com.dips.parser.dedupe.InvoiceIdCache;
import com.dips.parser.model.WatcherFrame;
import com.dips.parser.mq.RabbitPublisher;
import com.dips.parser.util.Sha256Utils;
import com.dips.parser.validator.ValidatorRmiClient;
import com.dips.parser.xml.InvoiceXmlParser;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.ValidationResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserPipelineService {
  private static final Logger logger = LoggerFactory.getLogger(ParserPipelineService.class);

  private final InvoiceXmlParser xmlParser;
  private final InvoiceIdCache invoiceIdCache;
  private final ValidatorRmiClient validatorRmiClient;
  private final RabbitPublisher rabbitPublisher;

  public ParserPipelineService(
      InvoiceXmlParser xmlParser,
      InvoiceIdCache invoiceIdCache,
      ValidatorRmiClient validatorRmiClient,
      RabbitPublisher rabbitPublisher) {
    this.xmlParser = xmlParser;
    this.invoiceIdCache = invoiceIdCache;
    this.validatorRmiClient = validatorRmiClient;
    this.rabbitPublisher = rabbitPublisher;
  }

  public void process(WatcherFrame frame) {
    try {
      if (!isIntegrityValid(frame.payload(), frame.sha256())) {
        rabbitPublisher.publishRejected(
            null,
            null,
            frame.fileName(),
            "HASH_MISMATCH",
            "Received hash does not match payload hash");
        logger.warn("Discarded {} due to HASH_MISMATCH", frame.fileName());
        return;
      }

      String invoiceId = xmlParser.extractInvoiceId(frame.payload());
      if (invoiceId == null || invoiceId.isBlank()) {
        rabbitPublisher.publishRejected(
            null, null, frame.fileName(), "XML_PARSE_ERROR", "Missing invoice id");
        logger.warn("Discarded {} due to missing invoice id", frame.fileName());
        return;
      }

      boolean reserved = invoiceIdCache.reserveIfNew(invoiceId);
      if (!reserved) {
        rabbitPublisher.publishRejected(
            null,
            invoiceId,
            frame.fileName(),
            "DUPLICATE_INVOICE_ID",
            "Invoice id already exists in 60-minute cache");
        logger.info("Duplicate invoice {} from file {}", invoiceId, frame.fileName());
        return;
      }

      Invoice invoice = xmlParser.parse(frame.payload());
      ValidationResult validationResult = validatorRmiClient.validate(invoice);

      if (validationResult.valid()) {
        rabbitPublisher.publishValidated(invoice, validationResult, frame.fileName());
        logger.info("Invoice {} validated and published", invoice.id());
      } else {
        rabbitPublisher.publishRejected(
            invoice,
            invoice.id(),
            frame.fileName(),
            "VALIDATION_FAILED",
            List.copyOf(validationResult.errors()));
        logger.info(
            "Invoice {} rejected with {} validation errors",
            invoice.id(),
            validationResult.errors().size());
      }
    } catch (Exception ex) {
      try {
        rabbitPublisher.publishRejected(
            null, null, frame.fileName(), "PARSER_INTERNAL_ERROR", ex.getMessage());
      } catch (Exception ignored) {
      }
      logger.error("Unexpected parser pipeline error for {}", frame.fileName(), ex);
    }
  }

  private boolean isIntegrityValid(byte[] payload, byte[] receivedHash) {
    byte[] calculated = Sha256Utils.hash(payload);
    if (calculated.length != receivedHash.length) {
      return false;
    }
    for (int i = 0; i < calculated.length; i++) {
      if (calculated[i] != receivedHash[i]) {
        return false;
      }
    }
    return true;
  }
}
