package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.client.impl.ClientResponseImpl;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Tags({@Tag(name = "Passthrough")})
public abstract class PassthroughProvider extends ApiProvider {
	
	public static final String PASSTHROUGH_API_ROOT = API_ROOT + "/passthrough";
	public static final String PASSTHROUGH_API_PLUGIN_ROOT = PASSTHROUGH_API_ROOT + "/plugin";
	
	
	@Getter
	@Inject
	@Location("tags/objView/history/searchResults")
	Template historyTemplate;
	
	protected Uni<Response> processHistoryResults(
		Uni<ObjectNode> searchUni,
		String acceptType,
		String searchFormId
	) {
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
								   .data("rootPrefix", this.getRootPrefix())
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
		TemplateInstance searchResultTemplate,
		String acceptType,
		String searchFormId,
		String otherModalId,
		String inputIdPrepend,
		String actionType
	) {
		if (MediaType.TEXT_HTML.equals(acceptType)) {
			return searchUni.map(
				(ObjectNode endResults)->{
					log.debug("Final result of search: {}", endResults);
					return Response.ok(
						searchResultTemplate
							.data("rootPrefix", this.getRootPrefix())
							.data("actionType", actionType)
							.data("searchFormId", searchFormId)
							.data("otherModalId", otherModalId)
							.data("inputIdPrepend", inputIdPrepend)
							.data("searchResults", endResults),
						MediaType.TEXT_HTML
					).build();
				});
		} else {
			return searchUni.map((output)->{
				log.debug("Final result of search: {}", output);
				return Response.ok(output).build();
			});
		}
		
	}
	
	protected Uni<Response> processSearchResults(
		Uni<ObjectNode> searchUni,
		Template searchResultTemplate,
		String acceptType,
		String searchFormId,
		String otherModalId,
		String inputIdPrepend,
		String actionType
	) {
		return this.processSearchResults(
			searchUni,
			searchResultTemplate.instance(),
			acceptType,
			searchFormId,
			otherModalId,
			inputIdPrepend,
			actionType
		);
	}
	
	protected Uni<Response> handleCall(Uni<?> uni) {
		return uni
				   .map(response -> {
					   if(response instanceof InputStream) {
						   return (StreamingOutput) output->{
							   IOUtils.copy((InputStream) response, output);
						   };
					   }
					   return response;
				   })
				   .map(response->{
						   if (response instanceof Response) {
							   return (Response) response;
						   }
						   return Response.ok(response).build();
					   }
				   )
				   .onFailure().recoverWithItem(this::handleApiError);
	}
	
	
	protected Response handleApiError(Throwable e) {
		log.debug("Handling API error: ", e);
		
		Response.ResponseBuilder output = Response.serverError();
		
		try {
			if (e instanceof ClientWebApplicationException) {
				Response response = ((ClientWebApplicationException) e).getResponse();
				String errorBody = new String(((ClientResponseImpl) ((ClientWebApplicationException) e).getResponse()).getEntityStream().readAllBytes(), StandardCharsets.UTF_8);
				
				output = Response.status(response.getStatus())
							 .entity(errorBody);
			}
		} catch(Throwable e2) {
			log.error("Error handling API error: {}", e, e2);
		}
		
		return output.build();
	}
}
