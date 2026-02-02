package tech.ebp.oqm.core.baseStation.service.printout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriFunction;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StoredSearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Named("PrintoutDataUtilService")
@ApplicationScoped
public class PrintoutDataSearchUtilService {

	@RestClient
	OqmCoreApiClientService client;

	public ObjectNode searchItemsInBlock(String auth, String db, String blockId, String storageType) {
		log.debug("Getting {} items in block {}", storageType, blockId);
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
		log.debug("Getting next items in block");
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
	
	public ResultsIterator getItemInBlockResultsIterator(String auth, String db, String blockId, String storageType){
		return new ResultsIterator(
			auth,
			db,
			this.searchItemsInBlock(auth, db, blockId, storageType),
			this::getItemsNextPage
		);
	}
	
	public ObjectNode getSingleStoredInBlockPage(String auth, String db, String itemId, String blockId){
		ObjectNode results;
		try {
			results = (ObjectNode) this.client.invItemStoredInBlockSearch(auth, db, itemId, blockId, new StoredSearch())
					   .subscribeAsCompletionStage()
					   .get();
		} catch(InterruptedException|ExecutionException e) {
			throw new RuntimeException(e);
		}
		
		int numResults = results.get("numResultsForEntireQuery").asInt();
		if(numResults == 0){
			return null;
		} else if (numResults > 1) {
			log.warn("Found more than one results for {} block in item {}", blockId, itemId);
			return null;
		}
		
		return (ObjectNode) results.get("results").get(0);
	}
	
	public ObjectNode searchStoredInBlock(String auth, String db, String itemId, String blockId){
		try {
			return (ObjectNode) this.client.invItemStoredInBlockSearch(auth, db, itemId, blockId, new StoredSearch())
									   .subscribeAsCompletionStage()
									   .get();
		} catch(InterruptedException|ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ResultsIterator searchStoredInBlockResultsIterator(String auth, String db, String itemId, String blockId){
		return new ResultsIterator(
			auth,
			db,
			this.searchStoredInBlock(auth, db, itemId, blockId),
			this::getItemsNextPage
		);
	}
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ResultsIterator implements Iterator<ObjectNode> {
		
		@NonNull
		private String auth;
		@NonNull
		private String db;
		
		@Getter
		@NonNull
		private ObjectNode curResults;
		@NonNull
		private TriFunction<String, String, ObjectNode, ObjectNode> operation;
		private boolean first = true;
		
		public ResultsIterator(
			String auth,
			String db,
			ObjectNode curResults,
			TriFunction<String, String, ObjectNode, ObjectNode> operation
			){
			this(auth, db, curResults, operation, true);
		}
		
		@Override
		public boolean hasNext() {
			boolean onLastPage = this.curResults.get("pagingCalculations").get("onLastPage").asBoolean();
			boolean hasNext = this.first || !onLastPage;
			
			log.debug("Has next? {} / first: {} on last page: {}",  hasNext, this.first, onLastPage);
			
			
			return hasNext;
		}
		
		@Override
		public ObjectNode next() {
			if(this.first){
				log.info("Was at first result from search");
				
				this.first = false;
				return curResults;
			}
			log.info("Getting next results from search");
			this.curResults = this.operation.apply(this.auth, this.db, this.curResults);
			
			return this.curResults;
		}
		
		public boolean hasResults(){
			boolean hasResults = !this.curResults.get("empty").asBoolean();
			log.debug("Has results from search? {}", hasResults);
			
			return hasResults;
		}
	}
	
}
