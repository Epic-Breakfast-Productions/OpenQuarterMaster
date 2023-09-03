package tech.ebp.oqm.baseStation.service.mongo.utils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Singleton;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;

import java.time.ZonedDateTime;


@Singleton
public class ZonedDateTimeCodec implements Codec<ZonedDateTime> {
	
	@Override
	public ZonedDateTime decode(BsonReader bsonReader, DecoderContext decoderContext) {
		try {
			return ObjectUtils.OBJECT_MAPPER.readValue(bsonReader.readString(), this.getEncoderClass());
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to decode ZonedDateTime field.", e);
		}
	}
	
	@Override
	public void encode(BsonWriter bsonWriter, ZonedDateTime zonedDateTime, EncoderContext encoderContext) {
		try {
			bsonWriter.writeString(ObjectUtils.OBJECT_MAPPER.writeValueAsString(zonedDateTime));
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to encode ZonedDateTime field.", e);
		}
	}
	
	@Override
	public Class<ZonedDateTime> getEncoderClass() {
		return ZonedDateTime.class;
	}
}
