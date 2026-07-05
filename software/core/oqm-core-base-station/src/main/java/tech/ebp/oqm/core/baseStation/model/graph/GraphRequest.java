package tech.ebp.oqm.core.baseStation.model.graph;

import java.time.OffsetDateTime;

public record GraphRequest(String dbIdOrName,
                           String itemId,
                           OffsetDateTime startDateTime,
                           OffsetDateTime endDateTime) {}
