package com.dips.watcher.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FrameBuilder {
  private FrameBuilder() {}

  public static byte[] buildFrame(byte[] xmlBytes, String fileName) {
    byte[] hash = sha256(xmlBytes);
    byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
    ByteBuffer buffer =
        ByteBuffer.allocate(4 + xmlBytes.length + hash.length + 4 + fileNameBytes.length)
            .order(ByteOrder.BIG_ENDIAN);
    buffer.putInt(xmlBytes.length);
    buffer.put(xmlBytes);
    buffer.put(hash);
    buffer.putInt(fileNameBytes.length);
    buffer.put(fileNameBytes);
    return buffer.array();
  }

  private static byte[] sha256(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm unavailable", e);
    }
  }
}
