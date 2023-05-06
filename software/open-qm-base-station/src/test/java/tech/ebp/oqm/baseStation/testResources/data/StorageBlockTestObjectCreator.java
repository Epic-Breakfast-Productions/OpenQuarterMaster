package tech.ebp.oqm.baseStation.testResources.data;



import com.beust.ah.A;
import org.apache.commons.codec.binary.Base64InputStream;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.media.ImageCreateRequest;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@ApplicationScoped
public class StorageBlockTestObjectCreator extends TestObjectCreator<StorageBlock> {
	private static final String TEST_IMAGE = "/testFiles/test_image.png";
	
	@Override
	public StorageBlock getTestObject() {
		StorageBlock block = new StorageBlock(
			faker.name().fullName(),
			faker.name().fullName(),
			faker.lorem().paragraph(),
			faker.locality().localeString(),
			null,
			new ArrayList<>(),
			new ArrayList<>()
		);
		
		return block;
	}
}
