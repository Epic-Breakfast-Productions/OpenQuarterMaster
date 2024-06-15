package tech.ebp.oqm.core.baseStation.service.modelTweak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

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
				(objectNodes, collection) -> Stream.concat(objectNodes.stream(), collection.stream()).toList()
			);
		}

		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();

		for (String storageBlockId : resultIdMap.keySet()) {
			uniJoinBuilder.add(getOqmCoreApiClient().storageBlockGet(apiToken, oqmDb, storageBlockId));
		}

		//returns a uni, not a response
		return uniJoinBuilder.joinAll()
			.andCollectFailures()
			.map((List<ObjectNode> resultList) -> {
				String newFieldName = key + "-labelText";
				for(ObjectNode curStorageBlock : resultList) {
					String curLabelText = curStorageBlock.get("labelText").asText();
					for(ObjectNode curResult : resultIdMap.get(curStorageBlock.get("id").asText())) {
						curResult.put(newFieldName, curLabelText);
					}
				}
				return searchResults;
			});
	}

}
