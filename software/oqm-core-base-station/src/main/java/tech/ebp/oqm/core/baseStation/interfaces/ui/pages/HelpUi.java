package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.interfaces.ui.UiProvider;
import tech.ebp.oqm.core.baseStation.utils.Roles;

import java.util.Map;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class HelpUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/help")
	Template help;
	
	@GET
	@Path("help")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	@Produces(MediaType.TEXT_HTML)
	public Response overview() {
		Response.ResponseBuilder responseBuilder = Response.ok(
			this.setupPageTemplate(help)
				.data("unitCategoryMap", Map.of())//UnitUtils.UNIT_CATEGORY_MAP) TODO:: this
//				.data("productProviderInfoList", this.productLookupService.getProductProviderInfo())
//				.data("supportedPageScanInfoList", this.productLookupService.getSupportedPageScanInfo())
//				.data("legoProviderInfoList", this.productLookupService.getLegoProviderInfo())
			,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
