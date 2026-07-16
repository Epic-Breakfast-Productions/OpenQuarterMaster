package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.model.graph.TimeRange;
import tech.ebp.oqm.core.baseStation.model.graph.Transactions;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TransactionMapper {

    private TransactionMapper(){}

    public static List<Transactions> mapTransactionsToArray(JsonNode jsonResponse) {
        if(jsonResponse.get("results") == null || jsonResponse.get("results").isEmpty()) {
            return new ArrayList<>();
        }
        jsonResponse = jsonResponse.get("results");
        List<Transactions> transactionsList = new ArrayList<>();
        for (JsonNode transaction : jsonResponse) {
            transactionsList.add(mapTransaction(transaction));
        }
        return transactionsList;
    }

    private static Transactions mapTransaction(JsonNode transactionNode) {
        Instant timestamp = ZonedDateTime.parse(transactionNode.get("timestamp").asText()).toInstant();
        int value = transactionNode
            .get("postApplyResults")
            .get("stats")
            .get("total")
            .get("value")
            .asInt();
        return new Transactions(timestamp, value);
    }

    static TimeRange normalizeTimeRange(OffsetDateTime start, OffsetDateTime end) {
        if (start != null && end != null) {
            return new TimeRange(start.atZoneSameInstant(ZoneOffset.UTC), end.atZoneSameInstant(ZoneOffset.UTC));
        }
        if (start != null) {
            return new TimeRange(start.atZoneSameInstant(ZoneOffset.UTC), ZonedDateTime.now(ZoneOffset.UTC));
        }
        if (end != null) {
            ZonedDateTime zonedDateTime = end.atZoneSameInstant(ZoneOffset.UTC);
            return new TimeRange(zonedDateTime.minusMonths(1), zonedDateTime);
        }
        return new TimeRange(null, null);
    }
}
