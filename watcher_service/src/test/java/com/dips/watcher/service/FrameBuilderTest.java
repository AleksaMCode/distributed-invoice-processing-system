package com.dips.watcher.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.jupiter.api.Test;

class FrameBuilderTest {

  @Test
  void buildFrameWritesLengthPayloadHashAndFilename() throws Exception {
    byte[] xmlBytes = "<invoice><id>1</id></invoice>".getBytes(StandardCharsets.UTF_8);
    String fileName = "invoice-1.xml";

    byte[] frame = FrameBuilder.buildFrame(xmlBytes, fileName);

    ByteBuffer buffer = ByteBuffer.wrap(frame).order(ByteOrder.BIG_ENDIAN);
    int payloadLength = buffer.getInt();
    assertEquals(xmlBytes.length, payloadLength);

    byte[] payload = new byte[payloadLength];
    buffer.get(payload);
    assertArrayEquals(xmlBytes, payload);

    byte[] hash = new byte[32];
    buffer.get(hash);
    byte[] expectedHash = MessageDigest.getInstance("SHA-256").digest(xmlBytes);
    assertArrayEquals(expectedHash, hash);

    int fileNameLength = buffer.getInt();
    assertEquals(fileName.getBytes(StandardCharsets.UTF_8).length, fileNameLength);

    byte[] fileNameBytes = new byte[fileNameLength];
    buffer.get(fileNameBytes);
    assertEquals(fileName, new String(fileNameBytes, StandardCharsets.UTF_8));

    assertEquals(0, buffer.remaining());
  }
}
