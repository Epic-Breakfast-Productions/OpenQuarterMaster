package com.ebp.openQuarterMaster.lib.core.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Jackson module to handle the Mongodb ObjectId in a reasonable manner
 */
public class MongoObjectIdModule extends TestableModule<ObjectId> {
	
	public MongoObjectIdModule() {
		super(
			ObjectId.class,
			new ObjectIdSerializer(),
			new ObjectIdDeserializer()
		);
	}
	
	public static class ObjectIdSerializer extends JsonSerializer<ObjectId> {
		@Override
		public void serialize(ObjectId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(value.toHexString());
		}
	}
	
	public static class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {
		
		@Override
		public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return new ObjectId(p.getValueAsString());
		}
	}
	
	
}
