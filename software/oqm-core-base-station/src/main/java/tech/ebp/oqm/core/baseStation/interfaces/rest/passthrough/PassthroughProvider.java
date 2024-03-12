package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Tags({@Tag(name = "Passthrough")})
public abstract class PassthroughProvider extends ApiProvider {
	public static final String PASSTHROUGH_API_ROOT = API_ROOT + "/passthrough";
	
	@Getter
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Getter
	@Inject
	@Location("tags/objView/history/searchResults")
	Template historyTemplate;
	
	protected Uni<Response> processHistoryResults(
		Uni<ObjectNode> searchUni,
		String acceptType,
		String searchFormId
	){
		if (MediaType.TEXT_HTML.equals(acceptType)) {
			return searchUni.call((ObjectNode results)->{
					if (results.get("empty").asBoolean()) {
						return Uni.createFrom().item(results);
						//					return Response.ok(
						//						historyTemplate
						//							.data("searchFormId", searchFormId)
						//							.data("searchResults", results),
						//						MediaType.TEXT_HTML
						//					).build();
					}
					
					Map<String, Optional<ObjectNode>> entityRefMap = new ConcurrentHashMap<>();
					for (JsonNode curResult : (ArrayNode) results.get("results")) {
						entityRefMap.put(curResult.get("entity").asText(), Optional.empty());
					}
					
					UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
					
					for (String curEntityId : entityRefMap.keySet()) {
						uniJoinBuilder.add(getOqmCoreApiClient().interactingEntityGetReference(getBearerHeaderStr(), curEntityId));
					}
					
					//returns a uni, not a response
					return uniJoinBuilder.joinAll()
							   .andCollectFailures()
							   .map((List<ObjectNode> resultList)->{
								   for (ObjectNode curEntityRef : resultList) {
									   entityRefMap.put(curEntityRef.get("id").asText(), Optional.of(curEntityRef));
								   }
								   
								   for (JsonNode curResult : (ArrayNode) results.get("results")) {
									   ((ObjectNode) curResult).set("entityRef", entityRefMap.get(curResult.get("entity").asText()).get());
								   }
								   return results;
								   
							   });
				})
					   .map((ObjectNode endResults)->{
						   log.debug("Final result of history search: {}", endResults);
						   return Response.ok(
							   historyTemplate
								   .data("searchFormId", searchFormId)
								   .data("searchResults", endResults),
							   MediaType.TEXT_HTML
						   ).build();
					   });
		} else {
			return searchUni.map((output)->{
				log.debug("Storage Block History search results: {}", output);
				return Response.ok(output).build();
			});
		}
		
	}
	
	protected Uni<Response> processSearchResults(
		Uni<ObjectNode> searchUni,
		Template searchResultTemplate,
		String acceptType,
		String searchFormId
	) {
		if (MediaType.TEXT_HTML.equals(acceptType)) {
			return searchUni.map(
				(ObjectNode endResults)->{
					log.debug("Final result of history search: {}", endResults);
					return Response.ok(
						searchResultTemplate
							.data("actionType", "select")
							.data("searchFormId", searchFormId)
							.data("searchResults", endResults),
						MediaType.TEXT_HTML
					).build();
				});
		} else {
			return searchUni.map((output)->{
				log.debug("Storage Block search results: {}", output);
				return Response.ok(output).build();
			});
		}
		
	}
}
