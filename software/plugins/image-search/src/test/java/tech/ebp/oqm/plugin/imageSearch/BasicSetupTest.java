package tech.ebp.oqm.plugin.imageSearch;
//TEST NEW FUNCTION
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.plugin.imageSearch.testResources.testClasses.RunningServerTest;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Objects;

@Slf4j
@QuarkusTest
public class BasicSetupTest extends RunningServerTest {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
	@Test
	public void testBasicSetup() throws IOException, URISyntaxException {

        //Parses testImages folder and gets a list of filenames
        File directory = new File((Objects.requireNonNull(BasicSetupTest.class.getClassLoader().getResource("testImages/"))).toURI());
        File[] fileList = directory.listFiles();
        Assertions.assertNotNull(fileList);
        String[] imageList = new String[fileList.length];
        for(int i = 0; i < fileList.length; i++){
            imageList[i] = fileList[i].getName();
        }

        //Makes an item for each image in testImages folder
        for(int i = 0; i < imageList.length; i++){
            uploadSingleImage(imageList[i], "test" + (i + 1) + ".jpg");
        }

        //uploadSingleImage("bin.jpg", "test1.jpg");
		/*
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
		*/

	}

    //takes in the name of an image in the temporary testImages folder
    //
    public void uploadSingleImage(String resourceLocation, String outputFilename) throws IOException {
        //uploads image object and generates image id
        String imageId;
        try(
           InputStream inputStream = BasicSetupTest.class.getClassLoader().getResourceAsStream("testImages/" + resourceLocation);
           ){
           FileUploadBody testImageUploadObj = FileUploadBody.builder()
                                                   .file(inputStream)
                                                   .fileName(resourceLocation)
                                                   .description(FAKER.lorem().sentence())
                                                   .source(resourceLocation)
                                                   .build();
           imageId = this.oqmCoreApiClientService.imageAdd(
                   this.serviceAccountService.getAuthString(),
                   "default",
                   testImageUploadObj
           ).subscribeAsCompletionStage().join().get("id").asText();
        }

        log.info("imageId: {}", imageId);

        //create an item object for the image
        ObjectNode item = JsonNodeFactory.instance.objectNode();
        item.put("name", FAKER.appliance().equipment() + "-" + outputFilename);
        item.put("storageType", "BULK");
        item.putObject("unit").put("string", "units");
        item.putArray("imageIds").add(imageId);

        //make item id
        ObjectNode newItem = this.oqmCoreApiClientService.invItemCreate(serviceAccountService.getAuthString(), "default", item).subscribeAsCompletionStage().join();
        log.info("item: {}", newItem);

        //check to make sure image object is good
        ObjectNode imageObj = this.oqmCoreApiClientService.imageGet(serviceAccountService.getAuthString(), "default", imageId).subscribeAsCompletionStage().join();
        log.info("imageObj: {}", imageObj);


        //Get image object data, don't fully understand this part
        InputStream response = this.oqmCoreApiClientService.imageGetRevisionData(
            this.serviceAccountService.getAuthString(),
            "default",
            imageId,
            "latest"
        ).await().indefinitely();
        log.info("Response: {}", response);

        //Pull the image data back down into a test image in a new folder
        try(
             OutputStream outputStream = new FileOutputStream("build/test-results/" + outputFilename);
        ){
            response.transferTo(outputStream);
        }

    }
	
	
}
