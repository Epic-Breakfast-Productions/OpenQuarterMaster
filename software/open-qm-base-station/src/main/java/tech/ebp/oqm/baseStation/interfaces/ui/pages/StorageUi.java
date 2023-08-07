package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.rest.printouts.PageOrientation;
import tech.ebp.oqm.baseStation.rest.printouts.PageSizeOption;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.StorageBlockSearch;
import tech.ebp.oqm.baseStation.service.mongo.ItemCategoryService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	
	
	@Inject
	Span span;
	
	@GET
	@Path("storage")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response storage(
		@BeanParam StorageBlockSearch storageBlockSearch
	) {
		SearchResult<StorageBlock> searchResults = this.storageBlockService.search(storageBlockSearch, true);
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(storage, span, this.getInteractingEntity(), searchResults)
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
