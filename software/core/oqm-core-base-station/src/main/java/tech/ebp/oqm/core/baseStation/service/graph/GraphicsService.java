package tech.ebp.oqm.core.baseStation.service.graph;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.model.graph.GraphRequest;
import tech.ebp.oqm.core.baseStation.model.graph.TimeRange;
import tech.ebp.oqm.core.baseStation.service.printout.PrintoutDataSearchUtilService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;

import java.io.IOException;

@ApplicationScoped
public class GraphicsService extends ApiProvider {

    private final GraphProvider graphProvider;
    private final PrintoutDataSearchUtilService printoutDataSearchUtilService;

    public GraphicsService(GraphProvider graphProvider, PrintoutDataSearchUtilService printoutDataSearchUtilService) {
        this.graphProvider = graphProvider;
        this.printoutDataSearchUtilService = printoutDataSearchUtilService;
    }

    public byte[] createGraph(@Valid GraphRequest graphRequest) throws IOException {
        TimeRange timeRange = TransactionMapper.normalizeTimeRange(graphRequest.getStartDateTime(), graphRequest.getEndDateTime());
        PrintoutDataSearchUtilService.ResultsIterator transactionsIterator = this.getTransactions(graphRequest.getDbIdOrName(), graphRequest.getItemId(), timeRange);
        return graphProvider.getGraph(transactionsIterator);
    }

    private PrintoutDataSearchUtilService.ResultsIterator getTransactions(String dbIdOrName, String itemId, TimeRange timeRange) {
        AppliedTransactionSearch search = new AppliedTransactionSearch();
        search.setInventoryItemId(itemId);
        search.setStartDateTime(timeRange.start());
        search.setEndDateTime(timeRange.end());

        return this.printoutDataSearchUtilService.getTransactionsIterator(
            this.getBearerHeaderStr(),
            dbIdOrName,
            itemId,
            search
        );
    }
}
