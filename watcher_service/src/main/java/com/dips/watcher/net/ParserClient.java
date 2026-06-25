package com.dips.watcher.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ParserClient {
  private static final int STATUS_SIZE = 3; // "ACK"

  private final String host;
  private final int port;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  public ParserClient(String host, int port, int connectTimeoutMs, int readTimeoutMs) {
    this.host = host;
    this.port = port;
    this.connectTimeoutMs = connectTimeoutMs;
    this.readTimeoutMs = readTimeoutMs;
  }

  public AckResponse send(byte[] frameBytes) throws IOException {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
      socket.setSoTimeout(readTimeoutMs);

      try (OutputStream outputStream = socket.getOutputStream();
          InputStream inputStream = socket.getInputStream()) {
        outputStream.write(frameBytes);
        outputStream.flush();
        socket.shutdownOutput();

        byte[] ackBytes = readAllBytes(inputStream);
        return parseAck(ackBytes);
      }
    }
  }

  private AckResponse parseAck(byte[] ackBytes) {
    if (ackBytes.length < STATUS_SIZE) {
      return new AckResponse(false, "", "");
    }

    String status = new String(ackBytes, 0, STATUS_SIZE, StandardCharsets.US_ASCII);
    String rest =
        new String(ackBytes, STATUS_SIZE, ackBytes.length - STATUS_SIZE, StandardCharsets.UTF_8)
            .trim();
    String filename = rest.replaceFirst("^[|:;,\\s]+", "");
    boolean acknowledged = "ACK".equals(status);

    return new AckResponse(
        acknowledged, filename, new String(ackBytes, StandardCharsets.UTF_8).trim());
  }

  private byte[] readAllBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int read;
    while ((read = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, read);
    }
    return baos.toByteArray();
  }
}
