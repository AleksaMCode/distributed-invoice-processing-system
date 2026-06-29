package com.dips.parser.validator;

import com.dips.parser.config.ParserConfig;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.ValidationResult;
import com.dips.validator.rmi.ValidatorService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ValidatorRmiClient {
  private final ParserConfig config;

  public ValidatorRmiClient(ParserConfig config) {
    this.config = config;
  }

  public ValidationResult validate(Invoice invoice) throws Exception {
    Registry registry =
        LocateRegistry.getRegistry(config.validatorRmiHost(), config.validatorRmiPort());
    ValidatorService validatorService =
        (ValidatorService) registry.lookup(config.validatorRmiBindName());
    return validatorService.validate(invoice);
  }
}
