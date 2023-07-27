package tech.ebp.oqm.baseStation.service.mongo.utils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;

import javax.inject.Singleton;
import javax.measure.Unit;


@Singleton
public class UnitCodec implements Codec<Unit> {
	
	@Override
	public Unit decode(BsonReader bsonReader, DecoderContext decoderContext) {
		try {
			return ObjectUtils.OBJECT_MAPPER.readValue(bsonReader.readString(), Unit.class);
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to decode unit field.", e);
		}
	}
	
	@Override
	public void encode(BsonWriter bsonWriter, Unit unit, EncoderContext encoderContext) {
		try {
			bsonWriter.writeString(ObjectUtils.OBJECT_MAPPER.writeValueAsString(unit));
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to encode unit field.", e);
		}
	}
	
	@Override
	public Class<Unit> getEncoderClass() {
		return Unit.class;
	}
}
