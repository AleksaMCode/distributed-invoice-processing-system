package com.dips.validator.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public record Item(String description, Integer quantity, BigDecimal unitPrice)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
