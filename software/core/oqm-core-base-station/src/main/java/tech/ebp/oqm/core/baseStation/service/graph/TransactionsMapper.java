package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.JsonNode;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.XYChart;
import tech.ebp.oqm.core.baseStation.model.Transactions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static List<Transactions> mapTransactionsToArray(JsonNode jsonResponse) {
        jsonResponse = jsonResponse.get("results");
        List<Transactions> transactionsList = new ArrayList<>();
        for (JsonNode transaction : jsonResponse) {
            transactionsList.add(mapTransaction(transaction));
        }
        return transactionsList;
    }

    private static Transactions mapTransaction(JsonNode transactionNode) {
        String timestamp = transactionNode.get("timestamp").asText();
        int value = transactionNode
            .get("postApplyResults")
            .get("stats")
            .get("total")
            .get("value")
            .asInt();
        return new Transactions(timestamp, value);
    }

    public static byte[] toByteArray(XYChart chart) throws IOException {
        ByteArrayOutputStream heapSvg = new ByteArrayOutputStream();
        VectorGraphicsEncoder.saveVectorGraphic(chart, heapSvg, VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
        return heapSvg.toByteArray();
    }
}
