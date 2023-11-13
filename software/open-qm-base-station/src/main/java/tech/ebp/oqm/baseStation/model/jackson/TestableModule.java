package tech.ebp.oqm.baseStation.model.jackson;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;

abstract class TestableModule<T> extends SimpleModule {
	
	
	@Getter
	private final JsonSerializer<T> serializer;
	@Getter
	private final JsonDeserializer<T> deserializer;
	
	public TestableModule(
		Class<T> clazz,
		JsonSerializer<T> serializer,
		JsonDeserializer<T> deserializer
	) {
		super();
		this.serializer = serializer;
		this.deserializer = deserializer;
		
		addSerializer(clazz, this.getSerializer());
		addDeserializer(clazz, this.getDeserializer());
	}
	
}
