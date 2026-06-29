package com.dips.parser.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Sha256Utils {
  private Sha256Utils() {}

  public static byte[] hash(byte[] data) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(data);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }
}
