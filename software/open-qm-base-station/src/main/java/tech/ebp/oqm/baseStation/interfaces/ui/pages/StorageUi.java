package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.ebp.oqm.baseStation.rest.printouts.PageOrientation;
import tech.ebp.oqm.baseStation.rest.printouts.PageSizeOption;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class StorageUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/storage")
	Template storage;
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	ItemCategoryService itemCategoryService;
	
	@GET
	@Path("storage")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response storage(
		@BeanParam StorageBlockSearch storageBlockSearch
	) {
		SearchResult<StorageBlock> searchResults = this.storageBlockService.search(storageBlockSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(storage, this.getInteractingEntity(), searchResults)
				.data("allowedUnitsMap", UnitUtils.UNIT_CATEGORY_MAP)
				.data("numStorageBlocks", storageBlockService.count())
				.data("storageService", storageBlockService)
				.data("itemCatsService", itemCategoryService)
				.data("searchObject", storageBlockSearch)
				.data("pageOrientationOptions", PageOrientation.values())
				.data("pageSizeOptions", PageSizeOption.values())
				.data("historySearchObject", new HistorySearch()),
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
