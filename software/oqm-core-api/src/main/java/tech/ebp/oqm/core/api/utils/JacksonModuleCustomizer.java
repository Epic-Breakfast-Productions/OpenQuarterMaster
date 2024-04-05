package tech.ebp.oqm.core.api.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

@Singleton
public class JacksonModuleCustomizer implements ObjectMapperCustomizer {
	
	public void customize(ObjectMapper mapper) {
		ObjectUtils.setupObjectMapper(mapper);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}
}
