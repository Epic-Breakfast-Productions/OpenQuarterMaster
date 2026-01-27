package tech.ebp.oqm.lib.core.api.quarkus.runtime.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Slf4j
@IfBuildProfile(anyOf = {"dev", "test"})
@ApplicationScoped
public class CoreApiDevDbManagementService {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
	@Inject
	ObjectMapper objectMapper;
	
	
	private int fileNum = 0;
	private int blockNum = 0;
	private int itemNum = 0;
	
	private FileUploadBody newFile(String type) {
		FileUploadBody.Builder builder = FileUploadBody.builder();
		
		builder.description("File " + (++fileNum) + " (" + type + ")");
		builder.source("populator");
		
		switch (type) {
			case "image":
				builder.fileName("image.jpg");
				builder.file(new ByteArrayInputStream(
					Base64.getDecoder().decode(
						"/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCADqATkDASIAAhEBAxEB/8QAFwABAQEBAAAAAAAAAAAAAAAAAAECA//EACQQAQEBAAIBBAMBAQEBAAAAAAABESExQQISUXFhgZGxocHw/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAH/xAAWEQEBAQAAAAAAAAAAAAAAAAAAEQH/2gAMAwEAAhEDEQA/AMriLyCKgg1gQwCgs4FTMOdutepjQak+FzMSVqgxZdRdPPIIvH5WzzGdBriphtTeAXg2ZjKA1pqKDUGZca3foBek8gFv8Ie3fKdA1qb8s7hoL6eLVt51FsAnql3Ut1M7AWbflLMDkEMX/F6/YjK/pADFQAUNA6alYagKk72m/j9p4Bq2fDDSYKLNXPNLoHE/NT6RYC31cJxZ3yWVM+aBYi/S2ZgiAsnYJx5D21vPmqrm3PTfpQQwyAC8JZvSKDni41ZrMuUVVl+Uz9w9v/1QWrZsZ5nFPHYH+JZyureQSF5M+fJ0CAfwRAVRBQA1DAWVUayoJUWoDpsxntPsueBV4+VxhdyAtv8AjOLGpIDMLbeGvbF4iozJfr/WukAVABAXAQXEAAASzVAZdO2WNordm+emFl7XcQSNZiFtv0C9w90nhJf4mA1u+GcJFwIyAqL/AOovwgGNfSRqdIrNa29M0gKCAojU9PAMjWXpckEJFNFEAAXEUBABYz6rZ0ureQc9vyt9XxDF2QAXtABcQAs0AZywkvluJbyipifas52DcyxjlZweAO0xri/hc+wZOEKIu6nSyeToVZyWXwvCg53gW81QQ7aTNAn5dGZJPs1UXURQAUEMCXQLZE93PRZ5hPTgNMrbIzKCm52LZwCs+2M8w2g3sjPuZAXb4IsMAUACzVUGM4/K+md6vEXUUyM5PDR0IxYe6ramih0VNBrS4xoqN8Q1BFQk3yqyAsioioAAKgDSJL4/jQIn5igLrPqtOuf6oOaxbMoAltUAhhIoJiiggrPu+AaOIxtAX3JbaAIaLwi4t9X4T3fg2AFtqcrUUarP20zUDAmqoE0WRBZPNVUVEAAAAVAC8kvih2DSKxOdBqs7Z0l0gI0mKAC4AuHE7ZtBriM+744QAAAAABAFsveIttBICyaikvy1+r/Cen5rWQHIBQa4rIDRqSl5qDWqziqgAAAATA7BpGdqXb2C2+J/UgAtRQBSQtkBWb6vhLbQAAAAAEBRAAAAAUbm+GZNdPxAP+ql2Tjwx7/wIgZ8iKvBk+CJoCXii9gaqZ/qqihAAAEVABGkBFUwBftNkZ3QW34QAAABFAQAVAAAAAARVkl8gs/43sk1jL45LvHArepk+E9XTG35oLqsmIKmLAEygKg0y1AFQBUXwgAAAoBC34S3UAAABAVAAAAAABAUQAVABdRQa1PcYyit2z58M8C4ouM2NXpOEGeWtNZUatiAIoAKIoCoAoG4C9MW6dgIoAIAAAAAAACKWAgL0CAAAALiANCKioNLgM1CrLihmTafkt1EF3SZ5ZVUW4mnIKvAi5fhEURVDWVQBRAAAAAAAAQFRVyAyulgAqCKlF8IqLsEgC9mGoC+IusqCrv5ZEUVOk1RuJfwSLOOkGFi4XPCoYYrNiKauosBGi9ICstM1UAAAAAAFQ0VcTBAXUGgIqGoKhKAzRRUQUAwxoSrGRpkQA/qiosOL9oJptMRRVZa0VUqSiChE6BqMgCwqKqIogAIAqKCKgKoogg0lBFuIKgAAAKNRlf2gqsftsEtZWoAAqAACKoMqAAeSoqp39kL2AqLOlE8rEBFQARYALhigrNC9gGmooLp4TweEQFFBFAECgIoAu0ifIAqAAA//9k="
						)
				));
				break;
			case "file":
				builder.fileName("file.txt");
				builder.file(new ByteArrayInputStream("This is a test file.".getBytes()));
				break;
		}
		
		return builder.build();
	}
	
	private ObjectNode newStorageBlock() {
		return objectMapper.createObjectNode()
				   .put("label", "Storage Block " + (++blockNum));
	}
	
	
	public String resetDb() {
		log.info("Resetting all OQM DB's.");
		
		this.oqmCoreApiClient.manageDbClearAll(this.serviceAccountService.getAuthString()).await().indefinitely();
		
		this.blockNum = 0;
		this.itemNum = 0;
		
		log.info("DONE resetting all OQM DB's.");
		
		return "OK";
	}
	
	public String resetAndPopulateDb(String db) {
		String resetResult = this.resetDb();
		
		log.info("Populating OQM DB: {}", db);
		//TODO:: populate from files?
		
		ObjectNode image1 = this.oqmCoreApiClient.imageAdd(this.serviceAccountService.getAuthString(), db, newFile("image")).await().indefinitely();
		ObjectNode image2 = this.oqmCoreApiClient.imageAdd(this.serviceAccountService.getAuthString(), db, newFile("image")).await().indefinitely();
		
		ObjectNode file1 = this.oqmCoreApiClient.fileAttachmentAdd(this.serviceAccountService.getAuthString(), db, newFile("file")).await().indefinitely();
		
		ArrayNode images = objectMapper.createArrayNode();
		images.add(image1.get("id"));
		images.add(image2.get("id"));
		
		
		
		ObjectNode storageBlock1 =
			this.oqmCoreApiClient.storageBlockAdd(this.serviceAccountService.getAuthString(), db, newStorageBlock().set("imageIds", images)).await().indefinitely();
		ObjectNode storageBlock2 = this.oqmCoreApiClient.storageBlockAdd(this.serviceAccountService.getAuthString(), db, newStorageBlock()).await().indefinitely();
		ObjectNode storageBlock3 = this.oqmCoreApiClient.storageBlockAdd(this.serviceAccountService.getAuthString(), db, newStorageBlock()).await().indefinitely();
		ObjectNode storageBlock4 =
			this.oqmCoreApiClient.storageBlockAdd(this.serviceAccountService.getAuthString(), db, newStorageBlock().set("parent", storageBlock3.get("id"))).await().indefinitely();
		ObjectNode storageBlock5 = this.oqmCoreApiClient.storageBlockAdd(
			this.serviceAccountService.getAuthString(), db,
			newStorageBlock().set("parent", storageBlock4.get("id"))
		).await().indefinitely();
		
		//TODO:: categories
		
		
		//TODO:: item - bulk
		//TODO:: item - amt list
		//TODO:: item - unique multi
		//TODO:: item - unique single
		
		//TODO:: checkouts
		
		log.info("DONE populating OQM DB: {}", db);
		return "OK";
	}
	
}
