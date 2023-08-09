package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.baseStation.service.productLookup.ProductLookupService;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Blocking
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class HelpUi extends UiProvider {
	
	@Inject
	@Location("webui/pages/help")
	Template overview;
	
	@Inject
	Span span;
	
	@Inject
	ProductLookupService productLookupService;
	
	@GET
	@Path("help")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public Response help() {
		TemplateInstance template;
		if (this.getInteractingEntity() == null) {
			template = this.setupPageTemplate(overview, span)
						   .data("navbar", "toLogin");
		} else {
			template = this.setupPageTemplate(overview, span, this.getInteractingEntity())
						   .data("navbar", "full");
		}
		template = template
					   .data("unitCategoryMap", UnitUtils.UNIT_CATEGORY_MAP)
					   .data("productProviderInfoList", this.productLookupService.getProductProviderInfo())
					   .data("supportedPageScanInfoList", this.productLookupService.getSupportedPageScanInfo())
					   .data("legoProviderInfoList", this.productLookupService.getLegoProviderInfo())
		;
		
		Response.ResponseBuilder responseBuilder = Response.ok(
			template,
			MediaType.TEXT_HTML_TYPE
		);
		
		return responseBuilder.build();
	}
	
}
