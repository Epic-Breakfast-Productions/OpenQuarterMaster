package tech.ebp.oqm.core.baseStation.model.graph;

import java.time.Instant;

public record TransactionGraphValue(Instant timestamp, double value) {}
