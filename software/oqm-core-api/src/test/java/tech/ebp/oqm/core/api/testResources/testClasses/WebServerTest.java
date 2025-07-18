package tech.ebp.oqm.core.api.testResources.testClasses;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

@Execution(ExecutionMode.CONCURRENT)
public abstract class WebServerTest {
	protected static final Faker FAKER = new Faker();
	public static final ObjectMapper OBJECT_MAPPER = ObjectUtils.OBJECT_MAPPER;
}
