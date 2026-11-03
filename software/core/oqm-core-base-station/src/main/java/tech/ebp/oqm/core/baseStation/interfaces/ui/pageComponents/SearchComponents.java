package tech.ebp.oqm.core.baseStation.interfaces.ui.pageComponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.model.searchObjects.HistorySearchComponentQuery;
import tech.ebp.oqm.core.baseStation.model.searchObjects.ItemStoredTransactionDropdownQuery;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import static tech.ebp.oqm.core.baseStation.interfaces.ui.pageComponents.PageComponentProvider.PAGE_COMPONENT_ROOT;

@Slf4j
@Path(PAGE_COMPONENT_ROOT + "/search")
@Tags({@Tag(name = "UI")})
@Produces(MediaType.TEXT_HTML)
@RequestScoped
public class SearchComponents extends PageComponentProvider {

	@Inject
	@Location("tags/objView/history/objHistoryView")
	Template historyViewTemplate;

	@GET
	@Path("history")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response history(
		@BeanParam HistorySearchComponentQuery query
	) {
		return Response.ok(
			this.historyViewTemplate
				.data("containerId", query.getId())
				.data("objectId", query.getId())
				.data("objectUrl", query.getObjectUrl())
				.data("accordion", query.isAccordion()),
			MediaType.TEXT_HTML_TYPE
		).build();
	}
}
