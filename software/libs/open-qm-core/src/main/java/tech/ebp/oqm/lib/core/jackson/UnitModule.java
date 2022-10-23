package tech.ebp.oqm.lib.core.jackson;

import tech.ebp.oqm.lib.core.units.UnitUtils;
import tech.ebp.oqm.lib.core.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.measure.Unit;
import java.io.IOException;

import static tech.ebp.oqm.lib.core.units.UnitUtils.unitFromString;

/**
 * Jackson module to handle Units within {@link UnitUtils#UNIT_LIST}
 */
public class UnitModule extends TestableModule<Unit> {
	
	public static final String STRING_TOKEN = "string";
	
	public UnitModule() {
		super(
			Unit.class,
			new UnitSerializer(),
			new UnitDeserializer()
		);
	}
	
	public static class UnitSerializer extends JsonSerializer<Unit> {
		
		@Override
		public void serialize(Unit value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (!UnitUtils.UNIT_LIST.contains(value)) {
				serializers.findValueSerializer(value.getClass()).serialize(value, gen, serializers);
			} else {
				ObjectNode node = Utils.OBJECT_MAPPER.createObjectNode();
				node.put(STRING_TOKEN, UnitUtils.stringFromUnit(value));
				node.put("name", value.getName());
				node.put("symbol", value.getSymbol());
				
				gen.writeTree(node);
			}
		}
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
