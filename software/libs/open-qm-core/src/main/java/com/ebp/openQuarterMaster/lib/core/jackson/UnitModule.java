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
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.measure.Unit;
import java.io.IOException;

import static com.ebp.openQuarterMaster.lib.core.UnitUtils.ALLOWED_UNITS;

/**
 * Jackson module to handle the Mongodb ObjectId in a reasonable manner
 */
public class UnitModule extends SimpleModule {
	
	public static final String STRING_TOKEN = "string";
	
	public UnitModule() {
		super();
		addSerializer(Unit.class, new UnitSerializer());
		addDeserializer(Unit.class, new UnitDeserializer());
	}
	
	public static class UnitSerializer extends JsonSerializer<Unit> {
		
		@Override
		public void serialize(Unit value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (!ALLOWED_UNITS.contains(value)) {
				serializers.findValueSerializer(value.getClass()).serialize(value, gen, serializers);
			} else {
				ObjectNode node = Utils.OBJECT_MAPPER.createObjectNode();
				node.put(STRING_TOKEN, value.toString());
				node.put("name", value.getName());
				node.put("symbol", value.getSymbol());
				
				gen.writeTree(node);
			}
		}
	}
	
	private static Unit<?> unitFromString(String unitStr) {
		for (Unit<?> curUnit : ALLOWED_UNITS) {
			if (curUnit.toString().equals(unitStr)) {
				return curUnit;
			}
		}
		return null;
	}
	
	public static class UnitDeserializer extends JsonDeserializer<Unit> {
		
		@Override
		public Unit<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			Unit<?> output = unitFromString(
				((ObjectNode) p.getCodec().readTree(p)).get(STRING_TOKEN).asText()
			);
			
			if (output == null) {
				output = (Unit<?>) ctxt.findNonContextualValueDeserializer(
					Utils.OBJECT_MAPPER.constructType(Unit.class)).deserialize(p, ctxt);
			}
			return output;
		}
	}
}
