package com.dips.validator.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record ValidationResult(boolean valid, List<String> errors, String validatorTimestamp)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
