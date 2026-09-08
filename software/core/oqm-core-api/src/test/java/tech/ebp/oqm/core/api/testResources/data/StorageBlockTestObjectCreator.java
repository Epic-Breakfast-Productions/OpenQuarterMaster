package tech.ebp.oqm.core.api.testResources.data;

import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@ApplicationScoped
public class StorageBlockTestObjectCreator extends TestObjectCreator<StorageBlock> {

	public static StorageBlock getNewStorageBlock(){
		StorageBlock block = new StorageBlock(
			faker.name().fullName(),
			faker.name().fullName(),
			faker.lorem().paragraph(),
			faker.theRoom().locations(),
			null,
			new ArrayList<>(),
			new LinkedHashSet<>(),
			new LinkedHashSet<>()
		);

		return block;
	}

	@Override
	public StorageBlock getTestObject() {
		return getNewStorageBlock();
	}
}
