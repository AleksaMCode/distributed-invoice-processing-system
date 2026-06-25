package com.dips.watcher.service;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class PathRouter {
  private final BlockingQueue<Path> queue;
  private final Set<Path> inFlightOrQueued;

  public PathRouter(BlockingQueue<Path> queue) {
    this.queue = queue;
    this.inFlightOrQueued = ConcurrentHashMap.newKeySet();
  }

  public void submit(Path path) throws InterruptedException {
    Path normalized = path.toAbsolutePath().normalize();
    boolean added = inFlightOrQueued.add(normalized);
    if (!added) {
      return;
    }
    try {
      queue.put(normalized);
    } catch (InterruptedException ex) {
      inFlightOrQueued.remove(normalized);
      throw ex;
    }
  }

  public void complete(Path path) {
    inFlightOrQueued.remove(path.toAbsolutePath().normalize());
  }
}
