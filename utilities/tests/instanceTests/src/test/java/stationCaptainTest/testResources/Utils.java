package stationCaptainTest.testResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.datafaker.Faker;

public class Utils {
	public static final Faker FAKER = new Faker();
	
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	static {
		OBJECT_MAPPER.registerModule(new JavaTimeModule());
	}
}
