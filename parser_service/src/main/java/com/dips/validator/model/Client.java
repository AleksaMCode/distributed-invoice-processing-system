package com.dips.validator.model;

import java.io.Serial;
import java.io.Serializable;

public record Client(String name, String ein, String email) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
