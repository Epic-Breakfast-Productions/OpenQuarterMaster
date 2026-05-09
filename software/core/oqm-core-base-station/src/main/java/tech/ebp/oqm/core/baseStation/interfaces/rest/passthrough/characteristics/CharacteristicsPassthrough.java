package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.characteristics;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough.PassthroughProvider;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.HistorySearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.ImageSearch;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.service.OqmCoreCharacteristicsService;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Path(PassthroughProvider.PASSTHROUGH_API_ROOT + "/characteristics/")
@Authenticated
@RequestScoped
public class CharacteristicsPassthrough extends PassthroughProvider {
	
	@Inject
	OqmCoreCharacteristicsService characteristicsService;
	
	@GET
	@Path("/runBy/logo")
	@Produces("image/*")
	public Uni<Response> getCharacteristicsRunByLogoImage() {
		return this.characteristicsService.characteristicsRunByLogo();
	}
	
	@GET
	@Path("/runBy/banner")
	@Produces("image/*")
	public Uni<Response> getCharacteristicsRunByBannerImage() {
		return this.characteristicsService.characteristicsRunByBanner();
	}
	
	@GET
	@Path("/uis/{category}/{id}/icon")
	@Produces("image/*")
	public Uni<Response> getUiIcon(
		@PathParam("category") String category,
		@PathParam("id") String id
	) {
		return this.characteristicsService.getUiIcon(category, id);
	}
	
}
