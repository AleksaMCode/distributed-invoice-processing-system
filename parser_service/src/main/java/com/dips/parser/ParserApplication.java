package com.dips.parser;

import com.dips.parser.config.ParserConfig;
import com.dips.parser.dedupe.RedisInvoiceIdCache;
import com.dips.parser.mq.RabbitPublisher;
import com.dips.parser.net.ParserTcpServer;
import com.dips.parser.net.WatcherFrameReader;
import com.dips.parser.service.ParserPipelineService;
import com.dips.parser.validator.ValidatorRmiClient;
import com.dips.parser.xml.InvoiceXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserApplication {
  private static final Logger logger = LoggerFactory.getLogger(ParserApplication.class);

  public static void main(String[] args) throws Exception {
    ParserConfig config = ParserConfig.load();

    try (RedisInvoiceIdCache invoiceIdCache = new RedisInvoiceIdCache(config);
        RabbitPublisher rabbitPublisher = new RabbitPublisher(config)) {

      ParserPipelineService pipelineService =
          new ParserPipelineService(
              new InvoiceXmlParser(),
              invoiceIdCache,
              new ValidatorRmiClient(config),
              rabbitPublisher);

      ParserTcpServer tcpServer =
          new ParserTcpServer(config, pipelineService, new WatcherFrameReader());
      tcpServer.start();
    } catch (Exception ex) {
      logger.error("Parser service failed to start or terminated unexpectedly", ex);
      throw ex;
    }
  }
}
