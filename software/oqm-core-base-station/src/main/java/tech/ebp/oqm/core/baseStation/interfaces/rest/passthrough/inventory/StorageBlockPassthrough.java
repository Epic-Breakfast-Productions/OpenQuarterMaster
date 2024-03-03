package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.common.headers.HeaderUtil;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants.STORAGE_BLOCK_ROOT_ENDPOINT;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/storage-block")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class StorageBlockPassthrough extends PassthroughProvider {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Getter
	@Inject
	@Location("tags/objView/history/searchResults")
	Template historyTemplate;
	
	private Uni<ObjectNode> addEntityReferences(ObjectNode results) {
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
		boolean hasResultWithParent = false;
		
		for (JsonNode curResult : (ArrayNode) results.get("results")) {
			if (curResult.get("hasParent").asBoolean()) {
				hasResultWithParent = true;
				uniJoinBuilder.add(
					oqmCoreApiClient.storageBlockGet(getBearerHeaderStr(), curResult.get("parent").asText())
						.invoke((ObjectNode storageBlock)->{
							((ObjectNode) curResult).set("parentLabel", storageBlock.get("labelText"));
						})
				);
			}
		}
		if (hasResultWithParent) {
			return uniJoinBuilder.joinAll()
					   .andCollectFailures()
					   .map((list)->{
						   return results;
					   });
		}
		return Uni.createFrom().item(results);
	}
	
	@POST
	public Uni<Response> addStorageBlock(ObjectNode newStorageBlock) {
		return this.oqmCoreApiClient.storageBlockAdd(this.getBearerHeaderStr(), newStorageBlock)
				   .map(output->Response.ok(output).build()
				   );
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Uni<Response> getStorageBlock(@BeanParam StorageBlockSearch storageBlockSearch) {
		return this.oqmCoreApiClient.storageBlockSearch(this.getBearerHeaderStr(), storageBlockSearch)
				   .map(output->
							Response.ok(output).build()
				   );
	}
	
	@GET
	@Path("{blockId}")
	public Uni<Response> getStorageBlock(@PathParam("blockId") String storageBlockId) {
		return this.oqmCoreApiClient.storageBlockGet(this.getBearerHeaderStr(), storageBlockId)
				   .map(output->
							Response.ok(output).build()
				   );
	}
	
	@GET
	@Path("{blockId}/history")
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> getStorageBlockHistory(
		@PathParam("blockId") String storageBlockId,
		@BeanParam HistorySearch historySearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId
	) {
		Uni<ObjectNode> searchUni = this.oqmCoreApiClient.storageBlockGetHistory(this.getBearerHeaderStr(), storageBlockId, historySearch);
		
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
						uniJoinBuilder.add(oqmCoreApiClient.interactingEntityGetReference(getBearerHeaderStr(), curEntityId));
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
	
	@GET
	@Path("/tree")
	@Produces({MediaType.APPLICATION_JSON})
	public Uni<Response> storageBlockTree(@QueryParam("onlyInclude") List<String> onlyInclude) {
		return this.oqmCoreApiClient.storageBlockTree(this.getBearerHeaderStr(), onlyInclude)
				   .map(output->
							Response.ok(output).build()
				   );
	}
	
	@PUT
	@Path("/{id}")
	public Uni<Response> storageBlockUpdate(@PathParam("id") String id, ObjectNode updates){
		return this.oqmCoreApiClient.storageBlockUpdate(this.getBearerHeaderStr(), id, updates)
				   .map(output->
							Response.ok(output).build()
				   );
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response>  storageBlockDelete(@PathParam("id") String id){
		return this.oqmCoreApiClient.storageBlockDelete(this.getBearerHeaderStr(), id)
				   .map(output->
							Response.ok(output).build()
				   );
	}
}
