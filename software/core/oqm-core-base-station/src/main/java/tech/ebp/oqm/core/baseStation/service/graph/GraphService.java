package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.model.graph.GraphRequest;
import tech.ebp.oqm.core.baseStation.model.graph.TimeRange;
import tech.ebp.oqm.core.baseStation.service.graph.xchart.ItemStockGraphService;
import tech.ebp.oqm.core.baseStation.service.printout.PrintoutDataSearchUtilService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;

import java.io.IOException;

@ApplicationScoped
public class GraphService {

	@ConfigProperty(name = "ui.defaults.search.defaultPageSize")
	int defaultPageSize;

	@Inject
	ItemStockGraphService graphProvider;
	@Inject
	PrintoutDataSearchUtilService printoutDataSearchUtilService;
	@RestClient
	OqmCoreApiClientService coreApiClientService;

	public byte[] createGraphItemStock(
		String userApiKey,
		@Valid GraphRequest graphRequest
	) throws IOException {
		TimeRange timeRange = TransactionMapper.normalizeTimeRange(graphRequest.getStartDateTime(), graphRequest.getEndDateTime());
		PrintoutDataSearchUtilService.ResultsIterator transactionsIterator = this.getTransactions(userApiKey, graphRequest, timeRange);

		ObjectNode item = this.coreApiClientService.invItemGet(
			userApiKey,
			graphRequest.getDbIdOrName(),
			graphRequest.getItemId()
		).await().indefinitely();

		return graphProvider.getGraph(item, transactionsIterator);
	}

	private PrintoutDataSearchUtilService.ResultsIterator getTransactions(
		String userApiKey,
		GraphRequest graphRequest, TimeRange timeRange
	) {
		AppliedTransactionSearch search = new AppliedTransactionSearch();
		search.setInventoryItemId(graphRequest.getItemId());
		search.setStartDateTime(timeRange.start());
		search.setEndDateTime(timeRange.end());
		search.setPageSize(this.defaultPageSize);

		return this.printoutDataSearchUtilService.getTransactionsIterator(
			userApiKey,
			graphRequest.getDbIdOrName(),
			graphRequest.getItemId(),
			search
		);
	}
}
