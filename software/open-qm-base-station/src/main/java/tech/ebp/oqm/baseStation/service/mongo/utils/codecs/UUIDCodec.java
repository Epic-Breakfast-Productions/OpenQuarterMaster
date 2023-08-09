package tech.ebp.oqm.baseStation.service.mongo.utils.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import jakarta.inject.Singleton;
import java.util.UUID;

@Singleton
public class UUIDCodec implements Codec<UUID> {
	
	@Override
	public UUID decode(BsonReader reader, DecoderContext decoderContext) {
		return UUID.fromString(reader.readString());
	}
	
	@Override
	public void encode(BsonWriter writer, UUID value, EncoderContext encoderContext) {
		writer.writeString(value.toString());
	}
	
	@Override
	public Class<UUID> getEncoderClass() {
		return UUID.class;
	}
}
