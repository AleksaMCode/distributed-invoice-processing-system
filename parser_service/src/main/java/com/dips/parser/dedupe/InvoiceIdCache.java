package com.dips.parser.dedupe;

public interface InvoiceIdCache extends AutoCloseable {
  boolean reserveIfNew(String invoiceId);

  @Override
  void close();
}
