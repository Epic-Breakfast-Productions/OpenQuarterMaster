package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.model.graph.TransactionGraphValue;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Slf4j
public class TransactionMapper {

    private TransactionMapper(){}

    public static List<TransactionGraphValue> mapTransactionsToArray(JsonNode jsonResponse) {
        if(jsonResponse.get("results") == null || jsonResponse.get("results").isEmpty()) {
            return new ArrayList<>();
        }
        jsonResponse = jsonResponse.get("results");
        List<TransactionGraphValue> transactionGraphValueList = new ArrayList<>();
        for (JsonNode transaction : jsonResponse) {
            transactionGraphValueList.add(mapTransaction(transaction));
        }
        return transactionGraphValueList;
    }

    private static TransactionGraphValue mapTransaction(JsonNode transactionNode) {
        Instant timestamp = ZonedDateTime.parse(transactionNode.get("timestamp").asText()).withZoneSameInstant(UTC).toInstant();
        double value = transactionNode
            .get("postApplyResults")
            .get("stats")
            .get("total")
            .get("value")
            .asDouble();
        return new TransactionGraphValue(timestamp, value);
    }

}
