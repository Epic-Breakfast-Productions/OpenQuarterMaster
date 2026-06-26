package tech.ebp.oqm.core.api.service.mongo.utils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.inject.Singleton;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.jsr310.InstantCodec;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Codec for {@link ZonedDateTime} objects.
 */
@Singleton
public class ZonedDateTimeCodec implements Codec<ZonedDateTime> {
	public static final String ORIGINAL_FIELD_NAME = "zdt";
	public static final String UTC_FIELD_NAME = "utc";
	public static final String MONGO_INSTANT_FIELD_NAME = "mi";

	@Override
	public ZonedDateTime decode(BsonReader bsonReader, DecoderContext decoderContext) {
		ZonedDateTime output = null;

		try {
			if(bsonReader.getCurrentBsonType() == BsonType.STRING) {
				output = ObjectUtils.OBJECT_MAPPER.readValue(bsonReader.readString(), this.getEncoderClass());
			} else {
				bsonReader.readStartDocument();
				while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
					String fieldName = bsonReader.readName();
					if (fieldName.equals(ORIGINAL_FIELD_NAME)) {
						String dtStr = bsonReader.readString();
						output = ObjectUtils.OBJECT_MAPPER.readValue(dtStr, this.getEncoderClass());
					} else if (fieldName.equals(UTC_FIELD_NAME)) {
						bsonReader.readString();//needed to clear object
					} else if (fieldName.equals(MONGO_INSTANT_FIELD_NAME)) {
						bsonReader.readDateTime();//needed to clear object
					}
				}
				bsonReader.readEndDocument();
			}
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to decode ZonedDateTime field.", e);
		}
		return output;
	}

	@Override
	public void encode(BsonWriter bsonWriter, ZonedDateTime zonedDateTime, EncoderContext encoderContext) {
		try {
			bsonWriter.writeStartDocument();

			bsonWriter.writeString(ORIGINAL_FIELD_NAME, ObjectUtils.OBJECT_MAPPER.writeValueAsString(zonedDateTime));

			ZonedDateTime utc = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
			bsonWriter.writeString(UTC_FIELD_NAME, ObjectUtils.OBJECT_MAPPER.writeValueAsString(utc));
			bsonWriter.writeDateTime(MONGO_INSTANT_FIELD_NAME, utc.toInstant().toEpochMilli());

			bsonWriter.writeEndDocument();
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to encode ZonedDateTime field.", e);
		}
	}

	@Override
	public Class<ZonedDateTime> getEncoderClass() {
		return ZonedDateTime.class;
	}
}
