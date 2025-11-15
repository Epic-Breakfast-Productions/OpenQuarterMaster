package tech.ebp.oqm.core.baseStation.service.printout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriFunction;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;

import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

@Slf4j
@Named("PrintoutDataUtilService")
@ApplicationScoped
public class PrintoutDataSearchUtilService {

	@RestClient
	OqmCoreApiClientService client;

	public ObjectNode getItemsInBlock(String auth, String db, String blockId, String storageType) {
		try {
			return this.client.invItemSearch(
				auth,
				db,
				InventoryItemSearch.builder()
					.inStorageBlocks(List.of(blockId))
					.storageTypes(List.of(storageType))
					.build()
			).subscribeAsCompletionStage().get();
		} catch(InterruptedException|ExecutionException e) {
			throw new RuntimeException("Failed to get inventory items from search.", e);
		}
	}
	
	public ObjectNode getItemsNextPage(String auth, String db, ObjectNode prevSearchResults){
		ObjectNode pagingCalculations = (ObjectNode) prevSearchResults.get("pagingCalculations");
		ObjectNode prevQuery = (ObjectNode) prevSearchResults.get("searchObject");
		
		List<String> storageBlocks = new ArrayList<>();
		for(JsonNode node : prevQuery.get("inStorageBlocks")){
			storageBlocks.add(node.asText());
		}
		List<String> types = new ArrayList<>();
		for(JsonNode node : prevQuery.get("storageTypes")){
			types.add(node.asText());
		}
		
		try {
			return this.client.invItemSearch(
				auth,
				db,
				InventoryItemSearch.builder()
					.inStorageBlocks(storageBlocks)
					.storageTypes(types)
					.pageNum(pagingCalculations.get("curPage").asInt() + 1)
					.pageSize(pagingCalculations.get("pageSize").asInt())
					.build()
			).subscribeAsCompletionStage().get();
		} catch(InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Iterator<ObjectNode> getItemInBlockResultsIterator(String auth, String db, String blockId, String storageType){
		return new ResultsIterator(
			auth,
			db,
			this.getItemsInBlock(auth, db, blockId, storageType),
			this::getItemsNextPage
		);
	}
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ResultsIterator implements Iterator<ObjectNode> {
		
		@NonNull
		private String auth;
		@NonNull
		private String db;
		
		@NonNull
		private ObjectNode curResults;
		@NonNull
		private TriFunction<String, String, ObjectNode, ObjectNode> operation;
		private boolean first;
		
		public ResultsIterator(
			String auth,
			String db,
			ObjectNode curResults,
			TriFunction<String, String, ObjectNode, ObjectNode> operation
			){
			this(auth, db, curResults, operation, false);
		}
		
		@Override
		public boolean hasNext() {
			return this.curResults == null || this.curResults.get("pagingCalculations").get("onLastPage").asBoolean();
		}
		
		@Override
		public ObjectNode next() {
			if(first){
				first = false;
				return curResults;
			}
			
			this.curResults = this.operation.apply(this.auth, this.db, this.curResults);
			
			return curResults;
		}
	}
	
}
