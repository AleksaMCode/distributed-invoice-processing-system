package com.dips.parser.model;

public record WatcherFrame(byte[] payload, byte[] sha256, String fileName) {}
