package tech.ebp.oqm.plugin.imageSearch.service.mongo.codecs;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.List;

public class FloatArrayCodec implements Codec<float[]> {
	
	@Override
	public void encode(BsonWriter writer, float[] value, EncoderContext encoderContext) {
		writer.writeStartArray();
		for (float f : value) {
			// MongoDB stores all numbers as doubles in BSON
			writer.writeDouble(f);
		}
		writer.writeEndArray();
	}
	
	@Override
	public float[] decode(BsonReader reader, DecoderContext decoderContext) {
		reader.readStartArray();
		List<Float> list = new ArrayList<>();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			// Read as double and cast back to float, accepting potential precision loss
			list.add((float) reader.readDouble());
		}
		reader.readEndArray();
		
		// Convert list back to native float array
		float[] array = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
	
	@Override
	public Class<float[]> getEncoderClass() {
		return float[].class;
	}
}