package com.dips.parser.net;

import com.dips.parser.model.WatcherFrame;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class WatcherFrameReader {
  public WatcherFrame read(InputStream inputStream) throws IOException {
    DataInputStream data = new DataInputStream(inputStream);

    int payloadLength = data.readInt();
    if (payloadLength < 0) {
      throw new IOException("Invalid payload length: " + payloadLength);
    }

    byte[] payload = readExact(data, payloadLength);
    byte[] hash = readExact(data, 32);

    int fileNameLength = data.readInt();
    if (fileNameLength < 0) {
      throw new IOException("Invalid file name length: " + fileNameLength);
    }
    byte[] fileNameBytes = readExact(data, fileNameLength);
    String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

    return new WatcherFrame(payload, hash, fileName);
  }

  private byte[] readExact(DataInputStream inputStream, int size) throws IOException {
    byte[] bytes = new byte[size];
    int offset = 0;
    while (offset < size) {
      int read = inputStream.read(bytes, offset, size - offset);
      if (read == -1) {
        throw new EOFException("Unexpected EOF while reading frame");
      }
      offset += read;
    }
    return bytes;
  }
}
