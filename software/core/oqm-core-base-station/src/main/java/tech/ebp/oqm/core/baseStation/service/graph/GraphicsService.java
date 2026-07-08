package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.model.graph.GraphRequest;
import tech.ebp.oqm.core.baseStation.model.graph.TimeRange;
import tech.ebp.oqm.core.baseStation.model.graph.Transactions;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;

import java.util.List;
import java.io.IOException;

@ApplicationScoped
public class GraphicsService extends ApiProvider {

    private final GraphProvider graphProvider;

    public GraphicsService(GraphProvider graphProvider) {
        this.graphProvider = graphProvider;
    }

    public byte[] createGraph(GraphRequest graphRequest) throws IOException {
        TimeRange timeRange = TransactionMapper.normalizeTimeRange(graphRequest.getStartDateTime(), graphRequest.getEndDateTime());
        ObjectNode jsonResponse = this.getTransactions(graphRequest.getDbIdOrName(), graphRequest.getItemId(), timeRange);
        List<Transactions> transactionsList = TransactionMapper.mapTransactionsToArray(jsonResponse);
        return graphProvider.getGraph(transactionsList);
    }

    private ObjectNode getTransactions(String dbIdOrName, String itemId, TimeRange timeRange) {
        AppliedTransactionSearch search = new AppliedTransactionSearch();
        search.setInventoryItemId(itemId);
        search.setStartDateTime(timeRange.start());
        search.setEndDateTime(timeRange.end());

        return this.getOqmCoreApiClient()
            .invItemStoredTransactionSearch(this.getBearerHeaderStr(), dbIdOrName, itemId, search)
            .await()
            .indefinitely();
    }
}
