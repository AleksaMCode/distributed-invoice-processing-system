package com.dips.watcher.net;

public record AckResponse(boolean acknowledged, String fileName, String raw) {}
