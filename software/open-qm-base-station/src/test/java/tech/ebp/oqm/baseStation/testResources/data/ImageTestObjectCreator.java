package tech.ebp.oqm.baseStation.testResources.data;



import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.time.StopWatch;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.baseStation.model.rest.media.ImageCreateRequest;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@ApplicationScoped
public class ImageTestObjectCreator extends TestObjectCreator<Image> {
	private static final String TEST_IMAGE = "/testFiles/test_image.png";
	
	@Override
	public Image getTestObject() {
		Base64InputStream base64Is = new Base64InputStream(ImageTestObjectCreator.class.getResourceAsStream(TEST_IMAGE), true);
		
		ImageCreateRequest icr = null;
		try {
			icr = new ImageCreateRequest(
				faker.commerce().productName(),
				faker.lorem().paragraph(),
				faker.internet().url(),
				"data:image/png;base64," + new String(base64Is.readAllBytes()),
				new ArrayList<>(),
				new HashMap<>()
			);
		} catch(IOException e) {
			throw new RuntimeException("FAILED to read test image.", e);
		}
		
		Image image = new Image(icr);
		
		return image;
	}
}
