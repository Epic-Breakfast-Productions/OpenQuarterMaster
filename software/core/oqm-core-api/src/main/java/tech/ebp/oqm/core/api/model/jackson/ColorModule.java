package tech.ebp.oqm.core.api.model.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.awt.*;
import java.io.IOException;

/**
 * Jackson module for serializing and deserializing {@link Color} objects.
 * <p>
 * Colors are serialized as hex strings (e.g., "#FF5733") and deserialized from
 * hex string format or standard color name strings supported by {@link Color#decode(String)}.
 * </p>
 */
public class ColorModule extends TestableModule<Color> {
	
	/**
	 * Converts a Color to its hex string representation.
	 *
	 * @param c the color to convert
	 * @return hex string in format "#RRGGBB"
	 */
	public static String toHexString(Color c){
		int R = c.getRed();
		int G = c.getGreen();
		int B = c.getBlue();
		
		String rgb = String.format("#%02X%02X%02X", R, G, B);
		return rgb;
	}
	
	/**
	 * Parses a hex string or color name into a Color object.
	 *
	 * @param c the string to parse (hex or color name)
	 * @return the Color object
	 * @throws NumberFormatException if the string is not a valid color format
	 */
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
	
	/**
	 * Serializer that writes Color as hex string.
	 */
	public static class ColorSerializer extends JsonSerializer<Color> {
		@Override
		public void serialize(Color c, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(toHexString(c));
		}
	}
	
	/**
	 * Deserializer that parses hex strings or color names into Color objects.
	 */
	public static class ColorDeserializer extends JsonDeserializer<Color> {
		@Override
		public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return toColor(p.getValueAsString());
		}
	}
	
}
