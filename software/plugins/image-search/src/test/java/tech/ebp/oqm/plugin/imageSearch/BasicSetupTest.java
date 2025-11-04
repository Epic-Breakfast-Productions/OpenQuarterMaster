package tech.ebp.oqm.plugin.imageSearch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.FileBackedOutputStream;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@QuarkusTest
public class BasicSetupTest extends RunningServerTest {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
	@Test
	public void testBasicSetup() throws IOException {
		
		//create image
		String imageId;
		try(
			InputStream is = BasicSetupTest.class.getClassLoader().getResourceAsStream("testImages/bin.jpg");
			){
			FileUploadBody testImageUpload = new FileUploadBody();
			testImageUpload.file = is;
			testImageUpload.fileName = "bin.jpg";
			testImageUpload.description = FAKER.lorem().sentence();
			testImageUpload.source = "bin.jpg";
			
			imageId = this.oqmCoreApiClientService.imageAdd(
				this.serviceAccountService.getAuthString(),
				"default",
				testImageUpload
			).subscribeAsCompletionStage().join();
		}
		imageId = imageId.replace("\"", "");
		log.info("imageId: {}", imageId);
		
		//create item with image
		ObjectNode item = JsonNodeFactory.instance.objectNode();
		item.put("name", FAKER.appliance().equipment());
		item.put("storageType", "BULK");
		item.putObject("unit").put("string", "units");
		
		item.putArray("imageIds").add(imageId);
		
		
		String result = this.oqmCoreApiClientService.invItemCreate(serviceAccountService.getAuthString(), "default", item).subscribeAsCompletionStage().join();
		log.info("Item id: {}", result);
		
		ObjectNode newItem = this.oqmCoreApiClientService.invItemGet(serviceAccountService.getAuthString(), "default", result.replace("\"", "")).subscribeAsCompletionStage().join();
		log.info("Item: {}", newItem);
		
		
		ObjectNode imageObj = this.oqmCoreApiClientService.imageGet(serviceAccountService.getAuthString(), "default", imageId).subscribeAsCompletionStage().join();
		log.info("imageObj: {}", imageObj);
		
		//get image object, data
		Response response = this.oqmCoreApiClientService.imageGetRevisionData(
			this.serviceAccountService.getAuthString(), "default",
			imageId,
			"latest"
		).subscribeAsCompletionStage().join();
		
		log.info("response: {}", response);
		try(
			OutputStream is = new FileOutputStream("build/test-results/test.png");
		){
			((InputStream)response.getEntity()).transferTo(is);
		}
		
	}
	
	
}
