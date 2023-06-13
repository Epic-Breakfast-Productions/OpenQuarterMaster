package tech.ebp.oqm.lib.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.awt.*;
import java.io.IOException;

/**
 * Jackson module to handle the Mongodb ObjectId in a reasonable manner (as its hex string)
 */
public class ColorModule extends TestableModule<Color> {
	
	public static String toHexString(Color c){
		int R = c.getRed();
		int G = c.getGreen();
		int B = c.getBlue();
		
		String rgb = String.format("#%02X%02X%02X", R, G, B);
		return rgb;
	}
	public static Color toColor(String c){
		return Color.decode(c);
	}
	
	public ColorModule() {
		super(
			Color.class,
			new ColorSerializer(),
			new ColorDeserializer()
		);
	}
	
	public static class ColorSerializer extends JsonSerializer<Color> {
		@Override
		public void serialize(Color c, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(toHexString(c));
		}
	}
	
	public static class ColorDeserializer extends JsonDeserializer<Color> {
		@Override
		public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return toColor(p.getValueAsString());
		}
	}
	
	
}
