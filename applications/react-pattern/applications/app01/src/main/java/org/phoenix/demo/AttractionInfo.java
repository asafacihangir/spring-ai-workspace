package org.phoenix.demo;

public record AttractionInfo(
    String name,
    String land,
    Integer heightRequirement,
    int currentWaitTime) {}
