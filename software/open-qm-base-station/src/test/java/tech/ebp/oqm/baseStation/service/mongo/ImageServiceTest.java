package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.data.ImageTestObjectCreator;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.lib.core.object.ObjectUtils;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.ListAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.TrackedItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class ImageServiceTest extends MongoHistoriedServiceTest<Image, ImageService> {
	
	ImageService imageService;
	
	ImageTestObjectCreator imageTestObjectCreator;
	
	@Inject
	ImageServiceTest(
		ImageService imageService,
		ImageTestObjectCreator imageTestObjectCreator,
		TestUserService testUserService
	) {
		this.imageService = imageService;
		this.imageTestObjectCreator = imageTestObjectCreator;
		this.testUserService = testUserService;
	}
	
	@Override
	protected Image getTestObject() {
		return imageTestObjectCreator.getTestObject();
	}
	
	@Test
	public void injectTest() {
		assertNotNull(imageService);
	}
	
	@Test
	public void listTest() {
		this.defaultListTest(this.imageService);
	}
	
	@Test
	public void countTest() {
		this.defaultCountTest(this.imageService);
	}
	
	@Test
	public void addTest() {
		this.defaultAddTest(this.imageService);
	}
	
	//TODO:: Test update
	
	@Test
	public void getObjectIdTest() {
		this.defaultGetObjectIdTest(this.imageService);
	}
	
	@Test
	public void getStringTest() {
		this.defaultGetStringTest(this.imageService);
	}
	
	@Test
	public void removeAllTest() {
		this.defaultRemoveAllTest(this.imageService);
	}
	
	@Ignore
	@Test
	public void testDeleteWithRelational(){
		//TODO
	}
}