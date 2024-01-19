package tech.ebp.oqm.baseStation.model.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;

import javax.measure.Unit;
import java.io.IOException;

import static tech.ebp.oqm.baseStation.model.units.UnitUtils.unitFromString;


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
				ObjectNode node = ObjectUtils.OBJECT_MAPPER.createObjectNode();
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
					ObjectUtils.OBJECT_MAPPER.constructType(Unit.class)).deserialize(p, ctxt);
			}
			return output;
		}
	}
}
