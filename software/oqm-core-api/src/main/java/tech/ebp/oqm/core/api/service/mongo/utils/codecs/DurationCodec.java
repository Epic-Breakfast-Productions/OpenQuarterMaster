package tech.ebp.oqm.core.api.service.mongo.utils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Singleton;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

import java.time.Duration;


@Singleton
public class DurationCodec implements Codec<Duration> {
	
	@Override
	public Duration decode(BsonReader bsonReader, DecoderContext decoderContext) {
		try {
			return ObjectUtils.OBJECT_MAPPER.readValue(bsonReader.readString(), this.getEncoderClass());
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to decode Duration field.", e);
		}
	}
	
	@Override
	public void encode(BsonWriter bsonWriter, Duration zonedDateTime, EncoderContext encoderContext) {
		try {
			bsonWriter.writeString(ObjectUtils.OBJECT_MAPPER.writeValueAsString(zonedDateTime));
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to encode Duration field.", e);
		}
	}
	
	@Override
	public Class<Duration> getEncoderClass() {
		return Duration.class;
	}
}
