package com.dips.parser.net;

import com.dips.parser.config.ParserConfig;
import com.dips.parser.model.WatcherFrame;
import com.dips.parser.service.ParserPipelineService;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserTcpServer {
  private static final Logger logger = LoggerFactory.getLogger(ParserTcpServer.class);

  private final ParserConfig config;
  private final ParserPipelineService pipelineService;
  private final WatcherFrameReader frameReader;

  public ParserTcpServer(
      ParserConfig config, ParserPipelineService pipelineService, WatcherFrameReader frameReader) {
    this.config = config;
    this.pipelineService = pipelineService;
    this.frameReader = frameReader;
  }

  public void start() throws Exception {
    ExecutorService workers = Executors.newFixedThreadPool(config.parserWorkers());

    try (ServerSocket serverSocket =
        new ServerSocket(config.parserPort(), 50, InetAddress.getByName(config.parserHost()))) {
      logger.info(
          "Parser socket server listening on {}:{} with workers={}",
          config.parserHost(),
          config.parserPort(),
          config.parserWorkers());

      while (!Thread.currentThread().isInterrupted()) {
        Socket clientSocket = serverSocket.accept();
        workers.submit(() -> handleConnection(clientSocket));
      }
    } finally {
      workers.shutdownNow();
    }
  }

  private void handleConnection(Socket clientSocket) {
    try (Socket socket = clientSocket) {
      WatcherFrame frame = frameReader.read(socket.getInputStream());
      AckWriter.writeAck(socket.getOutputStream(), frame.fileName());
      pipelineService.process(frame);
    } catch (Exception ex) {
      logger.error("Failed to process watcher connection", ex);
    }
  }
}
