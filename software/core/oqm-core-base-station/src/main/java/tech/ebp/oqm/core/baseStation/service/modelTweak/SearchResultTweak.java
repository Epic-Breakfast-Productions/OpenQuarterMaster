package tech.ebp.oqm.core.baseStation.service.modelTweak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.SearchObject;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class SearchResultTweak {
	
	@RestClient
	@Getter
	OqmCoreApiClientService oqmCoreApiClient;
	
	public Uni<ObjectNode> addStorageBlockLabelToSearchResult(ObjectNode searchResults, String oqmDb, String key, String apiToken) {
		if (searchResults.get("empty").asBoolean()) {
			return Uni.createFrom().item(searchResults);
		}
		
		Map<String, List<ObjectNode>> resultIdMap = new HashMap<>();
		for (JsonNode curResult : searchResults.get("results")) {
			//TODO:: this is probably bad for performance
			resultIdMap.merge(
				curResult.get(key).asText(),
				List.of((ObjectNode) curResult),
				(objectNodes, collection)->Stream.concat(objectNodes.stream(), collection.stream()).toList()
			);
		}
		
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
		
		for (String storageBlockId : resultIdMap.keySet()) {
			uniJoinBuilder.add(getOqmCoreApiClient().storageBlockGet(apiToken, oqmDb, storageBlockId));
		}
		
		//returns a uni, not a response
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map((List<ObjectNode> resultList)->{
					   String newFieldName = key + "-labelText";
					   for (ObjectNode curStorageBlock : resultList) {
						   String curLabelText = curStorageBlock.get("labelText").asText();
						   for (ObjectNode curResult : resultIdMap.get(curStorageBlock.get("id").asText())) {
							   curResult.put(newFieldName, curLabelText);
						   }
					   }
					   return searchResults;
				   });
	}
	
	public Uni<ObjectNode> addCreatedByInteractingEntityRefToCheckoutSearchResult(ObjectNode searchResults, String oqmDb, String apiToken) {
		if (searchResults.get("empty").asBoolean()) {
			return Uni.createFrom().item(searchResults);
		}
		
		UniJoin.Builder<Tuple2<ObjectNode, ObjectNode>> uniJoinBuilder = Uni.join().builder();
		for (JsonNode curResult : searchResults.get("results")) {
			uniJoinBuilder.add(
				getOqmCoreApiClient().itemCheckoutGetHistoryForObject(
						apiToken, oqmDb,
						curResult.get("id").asText(),
						HistorySearch.builder().eventTypes(List.of("CREATE")).build()
					)
					.chain((ObjectNode result)->{
						//TODO:: error check
						if (result.get("empty").asBoolean()) {
							throw new IllegalStateException("Cannot have a create search result with an empty result");
						}
						
						ObjectNode createEvent = (ObjectNode) result.get("results").get(0);
						
						return getOqmCoreApiClient().interactingEntityGetReference(apiToken, createEvent.get("entity").asText());
					})
					.map((ObjectNode entityRef)->{
						return Tuple2.of((ObjectNode) curResult, entityRef);
					})
			);
		}
		
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map((List<Tuple2<ObjectNode, ObjectNode>> resultList)->{
					   for (Tuple2<ObjectNode, ObjectNode> cur : resultList) {
						   ObjectNode searchResult = cur.getItem1();
						   ObjectNode entityRef = cur.getItem2();
						   
						   searchResult.set("creatorRef", entityRef);
					   }
					   return searchResults;
				   })
			;
	}
	
	public Uni<ObjectNode> addItemNameToSearchResult(ObjectNode searchResults, String oqmDb, String key, String apiToken) {
		if (searchResults.get("empty").asBoolean()) {
			return Uni.createFrom().item(searchResults);
		}
		
		Map<String, List<ObjectNode>> resultIdMap = new HashMap<>();
		for (JsonNode curResult : searchResults.get("results")) {
			//TODO:: this is probably bad for performance
			resultIdMap.merge(
				curResult.get(key).asText(),
				List.of((ObjectNode) curResult),
				(objectNodes, collection)->Stream.concat(objectNodes.stream(), collection.stream()).toList()
			);
		}
		
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
		
		for (String itemId : resultIdMap.keySet()) {
			uniJoinBuilder.add(getOqmCoreApiClient().invItemGet(apiToken, oqmDb, itemId));
		}
		
		//returns a uni, not a response
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map((List<ObjectNode> resultList)->{
					   String newFieldName = key + "-name";
					   for (ObjectNode curItem : resultList) {
						   String curName = curItem.get("name").asText();
						   for (ObjectNode curResult : resultIdMap.get(curItem.get("id").asText())) {
							   curResult.put(newFieldName, curName);
						   }
					   }
					   return searchResults;
				   });
	}
	
	public Uni<ObjectNode> addInteractingEntityRefToCheckoutSearchResult(ObjectNode searchResults, String apiToken) {
		if (searchResults.get("empty").asBoolean()) {
			return Uni.createFrom().item(searchResults);
		}
		
		Map<String, List<ObjectNode>> resultIdMap = new HashMap<>();
		for (JsonNode curResult : searchResults.get("results")) {
			ObjectNode curCheckoutFor = (ObjectNode) curResult.get("checkoutDetails").get("checkedOutFor");
			if (!"OQM_ENTITY".equals(curCheckoutFor.get("type").asText())) {
				continue;
			}
			
			log.debug("Checkout for: {}", curCheckoutFor);
			//TODO:: this is probably bad for performance
			resultIdMap.merge(
				curCheckoutFor.get("entity").asText(),
				List.of(curCheckoutFor),
				(objectNodes, collection)->Stream.concat(objectNodes.stream(), collection.stream()).toList()
			);
		}
		
		if (resultIdMap.isEmpty()) {
			return Uni.createFrom().item(searchResults);
		}
		
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
		
		for (String entityIds : resultIdMap.keySet()) {
			uniJoinBuilder.add(getOqmCoreApiClient().interactingEntityGetReference(apiToken, entityIds));
		}
		
		//returns a uni, not a response
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map((List<ObjectNode> resultList)->{
					   String newFieldName = "entityRef";
					   for (ObjectNode curEntityRef : resultList) {
						   for (ObjectNode curCheckoutFor : resultIdMap.get(curEntityRef.get("id").asText())) {
							   curCheckoutFor.set(newFieldName, curEntityRef);
						   }
					   }
					   return searchResults;
				   });
	}
	
	public Uni<ObjectNode> addStoredDetailToSearchResult(ObjectNode searchResults, String oqmDb, String key, String apiToken) {
		if (searchResults.get("empty").asBoolean()) {
			return Uni.createFrom().item(searchResults);
		}
		
		Map<String, List<ObjectNode>> resultIdMap = new HashMap<>();
		for (JsonNode curResult : searchResults.get("results")) {
			resultIdMap.merge(
				curResult.get(key).asText(),
				List.of((ObjectNode) curResult),
				(objectNodes, collection)->Stream.concat(objectNodes.stream().filter((curResult1)->curResult1.has(key)), collection.stream()).toList()
			);
		}
		
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
		
		for (String storageBlockId : resultIdMap.keySet()) {
			uniJoinBuilder.add(getOqmCoreApiClient().invItemStoredGet(apiToken, oqmDb, storageBlockId));
		}
		
		//returns a uni, not a response
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map((List<ObjectNode> resultList)->{
					   String newFieldName = key + "-labelText";
					   for (ObjectNode curStored : resultList) {
						   String curLabelText = curStored.get("labelText").asText();
						   for (ObjectNode curResult : resultIdMap.get(curStored.get("id").asText())) {
							   curResult.put(newFieldName, curLabelText);
						   }
					   }
					   return searchResults;
				   });
	}
	
	
	public Uni<ObjectNode> addCategoriesObjectsToSearchResult(ObjectNode searchResults, String oqmDb, String key, String apiToken) {
		if (searchResults.get("empty").asBoolean()) {
			return Uni.createFrom().item(searchResults);
		}
		String objSetKey = key+"-objs";
		
		// id -> results with that id?
		Map<String, List<ObjectNode>> resultIdMap = new HashMap<>();
		for (JsonNode curResult : searchResults.get("results")) {
			//TODO:: this is probably bad for performance
			
			for(JsonNode curCatNode : curResult.get(key)){
				String curCat = curCatNode.asText();
				
				resultIdMap.merge(
					curCat,
					List.of((ObjectNode) curResult),
					(objectNodes, collection)->Stream.concat(objectNodes.stream(), collection.stream()).toList()
				);
			}
			
			((ObjectNode)curResult).putArray(objSetKey);
		}
		
		if(resultIdMap.isEmpty()){
			return Uni.createFrom().item(searchResults);
		}
		
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
		
		for (String catId : resultIdMap.keySet()) {
			uniJoinBuilder.add(getOqmCoreApiClient().itemCatGet(apiToken, oqmDb, catId));
		}
		
		//returns a uni, not a response
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map((List<ObjectNode> resultList)->{
					   
					   for(ObjectNode curCategory : resultList){
						   for(ObjectNode curResult : resultIdMap.get(curCategory.get("id").asText())){
							   //TODO:: might need a mutex
							   ((ArrayNode)curResult.get(objSetKey)).add(curCategory);
						   }
					   }
					   
					   return searchResults;
				   });
	}
	
}
