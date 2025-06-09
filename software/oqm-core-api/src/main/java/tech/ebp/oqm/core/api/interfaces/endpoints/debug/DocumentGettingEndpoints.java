package tech.ebp.oqm.core.api.interfaces.endpoints.debug;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoCollection;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.RestInterface;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Path(
	EndpointProvider.ROOT_API_ENDPOINT_V1_DB_AWARE
	+ "/debug")
@Tags({
	@Tag(name = "Debug", description = "If you can see these and not debugging the source code, please report to the developers.")
})
@RequestScoped
@PermitAll
@IfBuildProfile("dev")
public class DocumentGettingEndpoints extends
	RestInterface
{
	
	@Getter
	@PathParam("oqmDbIdOrName")
	String
		oqmDbIdOrName;
	
	@Getter
	@Inject
	ObjectMapper
		objectMapper;
	
	@Inject
	InventoryItemService
		inventoryItemService;
	
	@Inject
	StorageBlockService
		storageBlockService;
	
	@SneakyThrows
	private ObjectNode docToObjNode(
		Document doc
	) {
		return (ObjectNode) objectMapper.readTree(
			doc.toJson());
	}
	
	private List<ObjectNode> dumpToJson(
		Collection<Document> docs
	) {
		return docs.stream()
				   .map(this::docToObjNode)
				   .toList();
	}
	
	@GET
	@Path("block/doc")
	public Response dumpBlockDocs() {
		MongoCollection<Document>
			itemDocs =
			this.storageBlockService.getDocumentCollection(
				this.getOqmDbIdOrName());
		
		return Response.ok(
				this.dumpToJson(
					itemDocs.find().into(new ArrayList<>())
				)
			)
				   .build();
	}
	
	@GET
	@Path("item/doc")
	public Response dumpItemDocs() {
		MongoCollection<Document>
			itemDocs =
			this.inventoryItemService.getDocumentCollection(
				this.getOqmDbIdOrName());
		
		return Response.ok(
				this.dumpToJson(
					itemDocs.find().into(new ArrayList<>())
				)
			)
				   .build();
	}
}
