package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ItemCategorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

import java.util.List;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/inventory/item-category")
@Authenticated
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class ItemCategoryPassthrough extends PassthroughProvider {
	
	@POST
	public Uni<Response> addItemCategory(ObjectNode newItemCategory) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCatAdd(this.getBearerHeaderStr(), this.getSelectedDb(), newItemCategory)
		);
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Uni<Response> getItemCategory(@BeanParam ItemCategorySearch itemCategorySearch) {
		//TODO:: handle HTML return for searches
		return this.handleCall(
			this.getOqmCoreApiClient().itemCatSearch(this.getBearerHeaderStr(), this.getSelectedDb(), itemCategorySearch)
		);
	}
	
	@GET
	@Path("{catId}")
	public Uni<Response> getItemCategory(@PathParam("catId") String itemCategoryId) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCatGet(this.getBearerHeaderStr(), this.getSelectedDb(), itemCategoryId)
		);
	}
	
	@GET
	@Path("{catId}/history")
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Uni<Response> getItemCategoryHistory(
		@PathParam("catId") String catId,
		@BeanParam HistorySearch historySearch,
		@HeaderParam("Accept") String acceptType,
		@HeaderParam("searchFormId") String searchFormId
	) {
		Uni<ObjectNode> searchUni = this.getOqmCoreApiClient().itemCatGetHistory(this.getBearerHeaderStr(), this.getSelectedDb(), catId, historySearch);
		return this.handleCall(
			this.processHistoryResults(searchUni, acceptType, searchFormId)
		);
	}
	
	@PUT
	@Path("/{id}")
	public Uni<Response> itemCategoryUpdate(@PathParam("id") String id, ObjectNode updates) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCatUpdate(this.getBearerHeaderStr(), this.getSelectedDb(), id, updates)
		);
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> itemCategoryDelete(@PathParam("id") String id) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCatDelete(this.getBearerHeaderStr(), this.getSelectedDb(), id)
		);
	}
	
	@GET
	@Path("/tree")
	@Produces({MediaType.APPLICATION_JSON})
	public Uni<Response> getItemCategoryTree(@QueryParam("onlyInclude") List<String> onlyInclude) {
		return this.handleCall(
			this.getOqmCoreApiClient().itemCatTree(this.getBearerHeaderStr(), this.getSelectedDb(), onlyInclude)
		);
	}
}
