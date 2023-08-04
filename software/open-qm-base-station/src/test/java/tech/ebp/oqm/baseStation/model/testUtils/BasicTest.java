package tech.ebp.oqm.baseStation.model.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;

public abstract class BasicTest {
	
	public static final Faker FAKER = new Faker();
	public static final ObjectMapper OBJECT_MAPPER = ObjectUtils.OBJECT_MAPPER;
}
