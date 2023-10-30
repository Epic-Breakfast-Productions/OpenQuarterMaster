package tech.ebp.oqm.baseStation.testResources.data;

import tech.ebp.oqm.baseStation.model.object.storage.storageBlock.StorageBlock;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;

@ApplicationScoped
public class StorageBlockTestObjectCreator extends TestObjectCreator<StorageBlock> {
	private static final String TEST_IMAGE = "/testFiles/test_image.png";
	
	@Override
	public StorageBlock getTestObject() {
		StorageBlock block = new StorageBlock(
			faker.name().fullName(),
			faker.name().fullName(),
			faker.lorem().paragraph(),
			faker.theRoom().locations(),
			null,
			new ArrayList<>(),
			new ArrayList<>()
		);
		
		return block;
	}
}
