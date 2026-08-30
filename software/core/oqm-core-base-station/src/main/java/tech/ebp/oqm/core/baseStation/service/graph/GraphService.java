package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.model.graph.GraphRequest;
import tech.ebp.oqm.core.baseStation.model.graph.ItemNameIterator;
import tech.ebp.oqm.core.baseStation.service.graph.xchart.ItemStockGraphService;
import tech.ebp.oqm.core.baseStation.service.printout.PrintoutDataSearchUtilService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		String oqmDbIdOrName,
		String userApiKey,
		@Valid GraphRequest graphRequest
	) throws IOException {
        List<ItemNameIterator> transactionItemNameIterator = new ArrayList<>();

        for (String itemId : graphRequest.getItemId()) {
            ObjectNode item = this.coreApiClientService.invItemGet(
                userApiKey,
                oqmDbIdOrName,
                itemId
            ).await().indefinitely();

            AppliedTransactionSearch search = new AppliedTransactionSearch();
            search.setInventoryItemId(itemId);
            search.setStartDateTime(graphRequest.getStartDateTime());
            search.setEndDateTime(graphRequest.getEndDateTime());
            search.setPageSize(this.defaultPageSize);

            PrintoutDataSearchUtilService.ResultsIterator transactionsIterator = this.printoutDataSearchUtilService.getTransactionsIterator(
                userApiKey,
                oqmDbIdOrName,
                itemId,
                search
            );
            //FIXME: key "name" and name
            transactionItemNameIterator.add(new ItemNameIterator(item.get("name").asText(), transactionsIterator));
        }

        return graphProvider.getGraph(transactionItemNameIterator);
	}
}
