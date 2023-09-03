package tech.ebp.oqm.baseStation.interfaces.endpoints.interactingEntity;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;

import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;

@Slf4j
@Path(ROOT_API_ENDPOINT_V1 + "/interacting-entity")
@Tags({@Tag(name = "Interacting Entities", description = "Endpoints for dealing with interacting entities.")})
@RequestScoped
public class InteractingEntityEndpoints extends EndpointProvider {
	
	@Inject
	@Location("tags/interactingEntityRef")
	Template interactingRefTemplate;
	
	@POST
	@Path("reference")
	@Operation(
		summary = "The html for a given interacting entity reference."
	)
	@APIResponse(
		responseCode = "200",
		description = "Got the currency."
	)
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance getInteractingEntityReferenceTemplate(
		@Valid InteractingEntityReference ref
	) {
		//TODO:: will need to change
		return interactingRefTemplate.data("entityRef", ref);
	}
	
	//TODO:: add search, get, history endpoints
}
