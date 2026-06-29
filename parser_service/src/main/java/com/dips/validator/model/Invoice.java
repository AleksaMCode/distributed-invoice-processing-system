package com.dips.validator.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record Invoice(String id, Client client, LocalDate date, String currency, List<Item> items)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
