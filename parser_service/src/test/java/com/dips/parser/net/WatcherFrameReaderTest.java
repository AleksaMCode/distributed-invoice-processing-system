package com.dips.parser.net;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dips.parser.model.WatcherFrame;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WatcherFrameReaderTest {
  private final WatcherFrameReader reader = new WatcherFrameReader();

  @Test
  void readParsesValidFrame() throws Exception {
    byte[] payload = "<invoice><id>1</id></invoice>".getBytes(StandardCharsets.UTF_8);
    byte[] hash = new byte[32];
    for (int i = 0; i < hash.length; i++) {
      hash[i] = (byte) i;
    }
    String fileName = "invoice-001.xml";
    byte[] frameBytes = buildFrameBytes(payload, hash, fileName);

    WatcherFrame frame = reader.read(new ByteArrayInputStream(frameBytes));

    assertArrayEquals(payload, frame.payload());
    assertArrayEquals(hash, frame.sha256());
    assertEquals(fileName, frame.fileName());
  }

  @Test
  void readThrowsWhenFrameIsTruncated() throws Exception {
    byte[] payload = "abc".getBytes(StandardCharsets.UTF_8);
    byte[] hash = new byte[32];
    String fileName = "a.xml";
    byte[] full = buildFrameBytes(payload, hash, fileName);
    byte[] truncated = new byte[full.length - 2];
    System.arraycopy(full, 0, truncated, 0, truncated.length);

    assertThrows(EOFException.class, () -> reader.read(new ByteArrayInputStream(truncated)));
  }

  @Test
  void readThrowsForNegativePayloadLength() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(-1);

    assertThrows(
        IOException.class, () -> reader.read(new ByteArrayInputStream(baos.toByteArray())));
  }

  private byte[] buildFrameBytes(byte[] payload, byte[] hash, String fileName) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);

    dos.writeInt(payload.length);
    dos.write(payload);
    dos.write(hash);
    dos.writeInt(fileNameBytes.length);
    dos.write(fileNameBytes);
    dos.flush();
    return baos.toByteArray();
  }
}
