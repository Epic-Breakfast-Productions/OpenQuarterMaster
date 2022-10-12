package tech.ebp.oqm.baseStation.service.mongo.utils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import javax.inject.Singleton;
import java.time.Duration;

import static tech.ebp.oqm.lib.core.Utils.OBJECT_MAPPER;


@Singleton
public class DurationCodec implements Codec<Duration> {
	
	@Override
	public Duration decode(BsonReader bsonReader, DecoderContext decoderContext) {
		try {
			return OBJECT_MAPPER.readValue(bsonReader.readString(), this.getEncoderClass());
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to decode ZonedDateTime field.", e);
		}
	}
	
	@Override
	public void encode(BsonWriter bsonWriter, Duration zonedDateTime, EncoderContext encoderContext) {
		try {
			bsonWriter.writeString(OBJECT_MAPPER.writeValueAsString(zonedDateTime));
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to encode ZonedDateTime field.", e);
		}
	}
	
	@Override
	public Class<Duration> getEncoderClass() {
		return Duration.class;
	}
}
