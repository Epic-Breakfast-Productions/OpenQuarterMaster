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
import tech.ebp.oqm.core.baseStation.model.searchObjects.ItemStoredTransactionDropdownQuery;
import tech.ebp.oqm.core.baseStation.utils.Roles;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import static tech.ebp.oqm.core.baseStation.interfaces.ui.pageComponents.PageComponentProvider.PAGE_COMPONENT_ROOT;

@Slf4j
@Path(PAGE_COMPONENT_ROOT + "/itemStoredTransaction")
@Tags({@Tag(name = "UI")})
@Produces(MediaType.TEXT_HTML)
@RequestScoped
public class TransactionComponents extends PageComponentProvider {
	
	@Inject
	@Location("tags/itemStored/transaction/buttons/transactionDropdown")
	Template transactionDropdownTemplate;
	
	@RestClient
	OqmCoreApiClientService coreApiClient;
	
	/**
	 * TODO:: beanparam for button shows, item/stored id
	 * @return
	 */
	@GET
	@Path("dropdown")
	@RolesAllowed(Roles.INVENTORY_VIEW)
	public Response dropdown(
		@BeanParam ItemStoredTransactionDropdownQuery query
	) {
		JsonNode item = null;
		JsonNode stored = null;
		
		if(
			query.getItem() != null ||
			query.getStored() != null
		){
			Uni<ObjectNode> itemUni = query.getItem() != null ? this.coreApiClient.invItemGet(this.getBearerHeaderStr(), this.getSelectedDb(), query.getItem()) : null;
			Uni<ObjectNode> storedUni = query.getStored() != null ? this.coreApiClient.invItemStoredGet(this.getBearerHeaderStr(), this.getSelectedDb(), query.getStored()) : null;
			
			if(itemUni != null){
				item = itemUni.await().indefinitely();
			}
			if(storedUni != null){
				stored = storedUni.await().indefinitely();
			}
		}
		
		//TODO:: logic to do button flags
		
		return Response.ok(
			this.transactionDropdownTemplate
				.data("item", item)
				.data("stored", stored),
			MediaType.TEXT_HTML_TYPE
		).build();
	}
}
