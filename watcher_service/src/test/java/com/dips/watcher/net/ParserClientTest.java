package com.dips.watcher.net;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ParserClientTest {

  @Test
  void sendParsesValidAckAndEchoedFilename() throws Exception {
    byte[] frame = "frame-bytes".getBytes(StandardCharsets.UTF_8);
    byte[] ackBytes = "ACK|invoice-1.xml".getBytes(StandardCharsets.UTF_8);

    try (ServerSocket serverSocket = new ServerSocket(0)) {
      CompletableFuture<byte[]> receivedFuture = new CompletableFuture<>();

      Thread serverThread =
          new Thread(
              () -> {
                try (Socket socket = serverSocket.accept();
                    InputStream in = socket.getInputStream()) {
                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  byte[] buffer = new byte[256];
                  int read;
                  while ((read = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                  }
                  receivedFuture.complete(baos.toByteArray());
                  socket.getOutputStream().write(ackBytes);
                  socket.getOutputStream().flush();
                } catch (Exception e) {
                  receivedFuture.completeExceptionally(e);
                }
              });
      serverThread.start();

      ParserClient client = new ParserClient("localhost", serverSocket.getLocalPort(), 2000, 2000);
      AckResponse response = client.send(frame);

      assertTrue(response.acknowledged());
      assertEquals("invoice-1.xml", response.fileName());
      assertEquals("ACK|invoice-1.xml", response.raw());
      assertArrayEquals(frame, receivedFuture.get(2, TimeUnit.SECONDS));
    }
  }

  @Test
  void sendReturnsNotAcknowledgedForShortAck() throws Exception {
    byte[] frame = "abc".getBytes(StandardCharsets.UTF_8);
    byte[] ackBytes = "A".getBytes(StandardCharsets.UTF_8);

    try (ServerSocket serverSocket = new ServerSocket(0)) {
      Thread serverThread =
          new Thread(
              () -> {
                try (Socket socket = serverSocket.accept()) {
                  socket.getInputStream().readAllBytes();
                  socket.getOutputStream().write(ackBytes);
                  socket.getOutputStream().flush();
                } catch (Exception ignored) {
                }
              });
      serverThread.start();

      ParserClient client = new ParserClient("localhost", serverSocket.getLocalPort(), 2000, 2000);
      AckResponse response = client.send(frame);

      assertFalse(response.acknowledged());
      assertEquals("", response.fileName());
      assertEquals("", response.raw());
    }
  }
}
