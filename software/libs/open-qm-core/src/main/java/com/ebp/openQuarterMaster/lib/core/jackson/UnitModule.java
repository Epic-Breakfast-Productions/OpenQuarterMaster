package com.ebp.openQuarterMaster.lib.core.jackson;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.measure.Unit;
import java.io.IOException;

/**
 * Jackson module to handle the Mongodb ObjectId in a reasonable manner
 */
public class UnitModule extends SimpleModule {
	
	public UnitModule() {
		super();
		addSerializer(Unit.class, new UnitSerializer());
		addDeserializer(Unit.class, new UnitDeserializer());
	}
	
	public static class UnitSerializer extends JsonSerializer<Unit> {
		
		@Override
		public void serialize(Unit value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (!UnitUtils.ALLOWED_UNITS.contains(value)) {
				serializers.findValueSerializer(value.getClass()).serialize(value, gen, serializers);
			} else {
				gen.writeString(UnitUtils.stringFromUnit(value));
			}
		}
	}
	
	public static class UnitDeserializer extends JsonDeserializer<Unit> {
		
		@Override
		public Unit<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			Unit<?> output = UnitUtils.unitFromString(p.getValueAsString());
			
			if (output == null) {
				output = (Unit<?>) ctxt.findNonContextualValueDeserializer(
					Utils.OBJECT_MAPPER.constructType(Unit.class)).deserialize(p, ctxt);
			}
			return output;
		}
	}
	
	
}
