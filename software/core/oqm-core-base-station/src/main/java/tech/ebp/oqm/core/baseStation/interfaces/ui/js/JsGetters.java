package tech.ebp.oqm.core.baseStation.interfaces.ui.js;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO:: rework these to standard page component calls?
 */
@Slf4j
@Path("/res/js/")
@Tags({@Tag(name = "JS Utilities")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class JsGetters {
	
	private static String carouselLines;
	private static String attInputLines;
	private static String keywordInputLines;
	private static String imageInputLines;
	private static String fileInputLines;
	private static String storedPricingInputLines;
	private static String storedPricingInputPriceLines;
	private static String assocLinkLinkInputLines;
	private static String identifierInputLines;
	private static String identifierAddedLines;
	private static String copyTextButtonLines;
	
	private static String itemCatBadgeLines;
	private static String attachedFileListLines;
	
	@Getter
	@HeaderParam("x-forwarded-prefix")
	Optional<String> forwardedPrefix;
	
	protected String getRootPrefix() {
		return this.forwardedPrefix.orElse("");
	}
	
	@Inject
	@Location("webui/js/Icons.js")
	Template icons;
	
	@Inject
	@Location("webui/js/constants.js")
	Template constants;
	
	@Inject
	@Location("webui/js/links.js")
	Template links;
	
	@Inject
	@Location("webui/js/carousel.js")
	Template carouselJs;
	
	
	@Inject
	@Location("webui/js/pageComponents.js")
	Template componentsJs;
	
	@Location("tags/carousel.html")
	Template carouselTemplate;
	@Location("tags/inputs/attInput.html")
	Template attInputTemplate;
	@Location("tags/inputs/keywordInput.html")
	Template keywordInputTemplate;
	@Location("tags/search/image/imageSelectFormInput.html")
	Template imageInputTemplate;
	@Location("tags/fileAttachment/fileAttachmentSelectFormInput.html")
	Template fileInputTemplate;
	@Location("tags/copyTextButton.html")
	Template copyButtonTemplate;
	@Location("tags/inputs/assocLink/assocLinkInputLink.qute.html")
	Template assocLinkInputLinkTemplate;
	@Location("tags/inputs/pricing/pricingInput.qute.html")
	Template storedPricingInputTemplate;
	@Location("tags/inputs/pricing/pricingInputPrice.qute.html")
	Template storedPricingInputPriceTemplate;
	@Location("tags/inputs/identifiers/associatedIdentifierInput.qute.html")
	Template identifierInputTemplate;
	@Location("tags/inputs/identifiers/addedIdentifier.qute.html")
	Template identifierAddedTemplate;
	@Location("tags/fileAttachment/FileAttachmentObjectView.html")
	Template fileAttachmentObjectViewTemplate;
	@Location("tags/objView/itemCatBadge.qute.html")
	Template itemCatBadgeTemplate;
	
	private String templateToEscapedJs(TemplateInstance templateInstance) {
		return templateInstance
				   .render()
				   .replaceAll("'", "\\\\'")
				   .replaceAll("\n", "\\\\\n")
			;
	}
	
	private String getCarouselLines() {
		if (carouselLines == null) {
			carouselLines = this.templateToEscapedJs(carouselTemplate.data("id", ""));
		}
		return carouselLines;
	}
	
	private String getAttInputLines() {
		if (attInputLines == null) {
			attInputLines = this.templateToEscapedJs(attInputTemplate.instance());
		}
		return attInputLines;
	}
	
	private String getKeywordInputLines() {
		if (keywordInputLines == null) {
			keywordInputLines = this.templateToEscapedJs(keywordInputTemplate.instance());
		}
		return keywordInputLines;
	}
	
	private String getImageInputLines() {
		if (imageInputLines == null) {
			imageInputLines = this.templateToEscapedJs(imageInputTemplate.instance());
		}
		return imageInputLines;
	}
	
	private String getFileInputLines() {
		if (fileInputLines == null) {
			fileInputLines = this.templateToEscapedJs(fileInputTemplate.instance());
		}
		return fileInputLines;
	}
	
	private String getCopyTextButtonLines() {
		if (copyTextButtonLines == null) {
			copyTextButtonLines = this.templateToEscapedJs(copyButtonTemplate.instance());
		}
		return copyTextButtonLines;
	}
	
	private String getStoredPricingInputLines() {
		if (storedPricingInputLines == null) {
			storedPricingInputLines = this.templateToEscapedJs(storedPricingInputTemplate.instance());
		}
		return storedPricingInputLines;
	}
	
	private String getStoredPricingInputPriceLines() {
		if (storedPricingInputPriceLines == null) {
			storedPricingInputPriceLines = this.templateToEscapedJs(storedPricingInputPriceTemplate.instance());
		}
		return storedPricingInputPriceLines;
	}
	
	private String getIdentifierInputLines() {
		if (identifierInputLines == null) {
			identifierInputLines = this.templateToEscapedJs(identifierInputTemplate.data("id", ""));
		}
		return identifierInputLines;
	}
	
	private String getIdentifierAddedLines() {
		if (identifierAddedLines == null) {
			identifierAddedLines = this.templateToEscapedJs(identifierAddedTemplate.data("rootPrefix", this.forwardedPrefix.orElse("")));
		}
		return identifierAddedLines;
	}
	
	private String getAttachedFileListLines() {
		if (attachedFileListLines == null) {
			attachedFileListLines = this.templateToEscapedJs(fileAttachmentObjectViewTemplate.instance());
		}
		return attachedFileListLines;
	}
	
	private String getItemCatBadgeLines() {
		if (itemCatBadgeLines == null) {
			itemCatBadgeLines = this.templateToEscapedJs(itemCatBadgeTemplate.instance());
		}
		return itemCatBadgeLines;
	}
	
	private String getAssocLinkLinkInputLines() {
		if (assocLinkLinkInputLines == null) {
			assocLinkLinkInputLines = this.templateToEscapedJs(assocLinkInputLinkTemplate.instance());
		}
		return assocLinkLinkInputLines;
	}
	
	@GET
	@Path("constants.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> constants() {
		return constants.data("rootPrefix", this.getRootPrefix()).createUni();
	}
	
	@GET
	@Path("Icons.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> icons() {
		return icons.instance().createUni();
	}
	
	@GET
	@Path("links.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> links() {
		return links.data("rootPrefix", this.getRootPrefix()).createUni();
	}
	
	@GET
	@Path("carousel.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> carousel() {
		return this.carouselJs
				   .data("carouselLines", this.getCarouselLines())
				   .createUni();
	}
	
	@GET
	@Path("pageComponents.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> components() {
		return this.componentsJs
				   .data("attInputLines", this.getAttInputLines())
				   .data("keywordInputLines", this.getKeywordInputLines())
				   .data("imageInputLines", this.getImageInputLines())
				   .data("fileInputLines", this.getFileInputLines())
				   .data("copyButtonLines", this.getCopyTextButtonLines())
				   .data("assocLinkLinkInputLines", this.getAssocLinkLinkInputLines())
				   .data("storedPricingInputLines", this.getStoredPricingInputLines())
				   .data("storedPricingInputPriceLines", this.getStoredPricingInputPriceLines())
				   .data("identifierInputLines", this.getIdentifierInputLines())
				   .data("identifierAddedLines", this.getIdentifierAddedLines())
				   .data("itemCatBadgeLines", this.getItemCatBadgeLines())
				   .data("attachedFileListLines", this.getAttachedFileListLines())
				   .createUni();
	}
}
