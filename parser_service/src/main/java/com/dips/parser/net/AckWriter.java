package com.dips.parser.net;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class AckWriter {
  private AckWriter() {}

  public static void writeAck(OutputStream outputStream, String fileName) throws IOException {
    String ack = "ACK|" + fileName;
    outputStream.write(ack.getBytes(StandardCharsets.UTF_8));
    outputStream.flush();
  }
}
