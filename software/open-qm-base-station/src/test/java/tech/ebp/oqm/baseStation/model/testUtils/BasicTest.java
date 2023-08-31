package tech.ebp.oqm.baseStation.model.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.datafaker.Faker;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;


public abstract class BasicTest {
	
	public static final Faker FAKER = new Faker();
	public static final ObjectMapper OBJECT_MAPPER = ObjectUtils.OBJECT_MAPPER;
}
