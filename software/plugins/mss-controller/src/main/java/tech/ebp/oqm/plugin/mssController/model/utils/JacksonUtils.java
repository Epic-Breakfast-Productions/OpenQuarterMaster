package tech.ebp.oqm.plugin.mssController.model.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class JacksonUtils  implements ObjectMapperCustomizer {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		setupObjectMapper(OBJECT_MAPPER);
	}

	public static void setupObjectMapper(ObjectMapper objectMapper) {
		objectMapper.registerModule(new JavaTimeModule());
	}


	@Override
	public void customize(ObjectMapper objectMapper) {
		setupObjectMapper(objectMapper);
	}
}
