
package tech.ebp.oqm.core.baseStation.interfaces.rest.components;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.utils.Roles;

@Slf4j
@Path("/api/pageComponents/")
@Tags({@Tag(name = "Units", description = "Endpoints for getting units.")})
@RolesAllowed(Roles.INVENTORY_VIEW)
@RequestScoped
public class KeywordAtts extends ApiProvider {
	
	
	@Getter
	@Inject
	@Location("tags/objView/objKeywordsView")
	Template keywordsView;
	
	@Getter
	@Inject
	@Location("tags/objView/objAttsView")
	Template attsView;
	
	@GET
	@Path("keywordDisplay")
	@Operation(
		summary = "The set of units compatible with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the units."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public TemplateInstance getKeywordsDisplay() {
		return this.keywordsView.instance();
	}
	
	@GET
	@Path("attDisplay")
	@Operation(
		summary = "The set of units compatible with."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the units."
	)
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public TemplateInstance getAttsDisplay() {
		return this.attsView.instance();
	}
}
