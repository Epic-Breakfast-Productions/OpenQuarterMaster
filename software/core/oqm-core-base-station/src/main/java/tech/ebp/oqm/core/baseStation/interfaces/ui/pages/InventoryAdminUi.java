package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.Map;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class InventoryAdminUi extends UiProvider {
	
	@Getter
	@Inject
	@Location("webui/pages/inventoryAdmin")
	Template pageTemplate;
	
	@RestClient
	OqmCoreApiClientService coreApiClient;
	
	@GET
	@Path("inventoryAdmin")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Uni<Response> invAdminPage() {
		
		return this.getUni(
			Map.of(
				"unitDerivisionTypes", this.coreApiClient.unitGetDeriveTypes(this.getBearerHeaderStr()),
				"unitDimensions", this.coreApiClient.unitGetDimensions(this.getBearerHeaderStr()),
				"customUnits", this.coreApiClient.unitCustomGetAll(this.getBearerHeaderStr()),
				"allUnitMap", this.coreApiClient.unitGetAll(this.getBearerHeaderStr())
			)
		);
	}
	
}
