package com.dips.watcher.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.Test;

class PathRouterTest {

  @Test
  void submitEnqueuesPathOnlyOnceUntilComplete() throws Exception {
    BlockingQueue<Path> queue = new ArrayBlockingQueue<>(10);
    PathRouter router = new PathRouter(queue);
    Path path = Path.of("invoices", "inbox", "a.xml");

    router.submit(path);
    router.submit(path);

    assertEquals(1, queue.size());
  }

  @Test
  void completeAllowsSamePathToBeQueuedAgain() throws Exception {
    BlockingQueue<Path> queue = new ArrayBlockingQueue<>(10);
    PathRouter router = new PathRouter(queue);
    Path path = Path.of("invoices", "inbox", "b.xml");

    router.submit(path);
    Path queued = queue.take();
    router.complete(queued);
    router.submit(path);

    assertEquals(1, queue.size());
  }
}
