package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

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
	
	@Getter
	@Inject
	@Location("tags/search/storage/searchResults")
	Template searchResultTemplate;
	
	@POST
	public Uni<Response> addStorageBlock(ObjectNode newStorageBlock) {
		return this.handleCall(
			this.getOqmCoreApiClient().storageBlockAdd(this.getBearerHeaderStr(), this.getSelectedDb(), newStorageBlock)
		);
	}
	
	@Path("bulk")
	@POST
	public Uni<Response> addStorageBlocksBulk(ArrayNode newStorageBlocks) {
		return this.handleCall(
			this.getOqmCoreApiClient().storageBlockAddBulk(this.getBearerHeaderStr(), this.getSelectedDb(), newStorageBlocks)
		);
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> search(
		@BeanParam StorageBlockSearch storageBlockSearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId,
		@HeaderParam("otherModalId") String otherModalId,
		@HeaderParam("inputIdPrepend") String inputIdPrepend
	) {
		return this.handleCall(
			this.processSearchResults(
				this.getOqmCoreApiClient().storageBlockSearch(this.getBearerHeaderStr(), this.getSelectedDb(), storageBlockSearch).call((ObjectNode results)->{
					return addParentLabelsToSearchResults(results, "labelText", this.getOqmCoreApiClient()::storageBlockGet);
				}),
				this.searchResultTemplate,
				acceptType,
				searchFormId,
				otherModalId,
				inputIdPrepend,
				"select"
			)
		);
	}
	
	@GET
	@Path("{blockId}")
	public Uni<Response> getStorageBlock(@PathParam("blockId") String storageBlockId) {
		return this.handleCall(
			this.getOqmCoreApiClient().storageBlockGet(this.getBearerHeaderStr(), this.getSelectedDb(), storageBlockId)
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
		Uni<ObjectNode> searchUni = this.getOqmCoreApiClient().storageBlockGetHistory(this.getBearerHeaderStr(), this.getSelectedDb(), storageBlockId, historySearch);
		return this.handleCall(
			this.processHistoryResults(searchUni, acceptType, searchFormId)
		);
	}
	
	@GET
	@Path("/tree")
	@Produces({MediaType.APPLICATION_JSON})
	public Uni<Response> storageBlockTree(@QueryParam("onlyInclude") List<String> onlyInclude) {
		return this.handleCall(
			this.getOqmCoreApiClient().storageBlockTree(this.getBearerHeaderStr(), this.getSelectedDb(), onlyInclude)
		);
	}
	
	@PUT
	@Path("/{id}")
	public Uni<Response> storageBlockUpdate(@PathParam("id") String id, ObjectNode updates) {
		return this.handleCall(
			this.getOqmCoreApiClient().storageBlockUpdate(this.getBearerHeaderStr(), this.getSelectedDb(), id, updates)
		);
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> storageBlockDelete(@PathParam("id") String id) {
		return this.handleCall(
			this.getOqmCoreApiClient().storageBlockDelete(this.getBearerHeaderStr(), this.getSelectedDb(), id)
		);
	}
}
