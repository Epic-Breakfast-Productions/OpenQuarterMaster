package tech.ebp.oqm.lib.core.api.quark.quarkus.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.AllInfo;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.service.OqmCoreCharacteristicsService;

import jakarta.inject.Inject;

public class CoreCharacteristicsLibBasicTest {
	
	// Start unit test with your extension loaded
	@RegisterExtension
	static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
												.withConfiguration("""
													oqm.core.characteristics.serviceCategory=core
													oqm.core.characteristics.serviceId=fooService
													""")
												.setArchiveProducer(()->ShrinkWrap.create(JavaArchive.class));
	
	@Inject
	OqmCoreCharacteristicsService coreCharacteristicsService;
	
	@Test
	public void writeYourOwnUnitTest() throws InterruptedException {
		AllInfo info = this.coreCharacteristicsService.allInfo().await().indefinitely();
		
		System.out.println("All info: " + info.toString());
		
		//        		Thread.sleep(180_000);
		
		Assertions.assertNotNull(info);
	}
}
