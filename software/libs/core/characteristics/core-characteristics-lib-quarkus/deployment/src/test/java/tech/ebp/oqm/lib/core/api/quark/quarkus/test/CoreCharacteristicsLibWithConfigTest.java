package tech.ebp.oqm.lib.core.api.quark.quarkus.test;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.AllInfo;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.service.OqmCoreCharacteristicsService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoreCharacteristicsLibWithConfigTest {
	
	@RegisterExtension
	static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
												.withConfiguration("""
													oqm.core.characteristics.serviceCategory=core
													oqm.core.characteristics.serviceId=fooService
													oqm.core.characteristics.devservices.devData.characteristics.title=foo
													oqm.core.characteristics.devservices.devData.characteristics.motd=bar
													oqm.core.characteristics.devservices.devData.characteristics.runBy.name=devs
													oqm.core.characteristics.devservices.devData.characteristics.runBy.email=devs@dev.dev
													oqm.core.characteristics.devservices.devData.characteristics.runBy.phone=8675309
													oqm.core.characteristics.devservices.devData.characteristics.runBy.website=http://dev.dev
													oqm.core.characteristics.devservices.devData.characteristics.runBy.haveLogoImg=true
													oqm.core.characteristics.devservices.devData.characteristics.runBy.haveBannerImg=true
													""")
												.setArchiveProducer(()->ShrinkWrap.create(JavaArchive.class));
	
	@Inject
	OqmCoreCharacteristicsService coreCharacteristicsService;
	
	@Test
	public void writeYourOwnUnitTest() throws InterruptedException, IOException {
		AllInfo info = this.coreCharacteristicsService.allInfo().await().indefinitely();
		
		System.out.println("All info: " + info.toString());
		
		//        		Thread.sleep(180_000);
		
		Assertions.assertNotNull(info);
		
		Assertions.assertNotNull(info.getCharacteristics());
		Assertions.assertEquals("foo", info.getCharacteristics().getTitle());
		Assertions.assertEquals("bar", info.getCharacteristics().getMotd());
		
		Assertions.assertNotNull(info.getCharacteristics().getRunBy());
		assertTrue(info.getCharacteristics().hasRunBy());
		Assertions.assertEquals("devs", info.getCharacteristics().getRunBy().getName());
		Assertions.assertEquals("devs@dev.dev", info.getCharacteristics().getRunBy().getEmail());
		Assertions.assertEquals("8675309", info.getCharacteristics().getRunBy().getPhone());
		Assertions.assertEquals("http://dev.dev", info.getCharacteristics().getRunBy().getWebsite());
		
		assertTrue(info.getCharacteristics().getRunBy().isHasLogoImg());
		assertTrue(info.getCharacteristics().getRunBy().isHasBannerImg());
		
		
		String logoData = IOUtils.toString(this.coreCharacteristicsService.characteristicsRunByLogo().await().indefinitely().readEntity(InputStream.class), StandardCharsets.UTF_8.name());
		assertNotNull(logoData);
		assertFalse(logoData.isBlank());
		System.out.println("Logo data length: " + logoData.length());
		
		String bannerData = IOUtils.toString(this.coreCharacteristicsService.characteristicsRunByBanner().await().indefinitely().readEntity(InputStream.class), StandardCharsets.UTF_8.name());
		assertNotNull(bannerData);
		assertFalse(bannerData.isBlank());
		System.out.println("Banner data length: " + bannerData.length());
		
	}
}
