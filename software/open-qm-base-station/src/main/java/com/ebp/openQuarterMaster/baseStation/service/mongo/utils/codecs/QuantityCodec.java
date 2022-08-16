package com.ebp.openQuarterMaster.baseStation.service.mongo.utils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import javax.inject.Singleton;
import javax.measure.Quantity;

import static com.ebp.openQuarterMaster.lib.core.Utils.OBJECT_MAPPER;

@Singleton
public class QuantityCodec implements Codec<Quantity> {
	
	@Override
	public Quantity decode(BsonReader bsonReader, DecoderContext decoderContext) {
		try {
			return OBJECT_MAPPER.readValue(bsonReader.readString(), Quantity.class);
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to decode unit field.", e);
		}
	}
	
	@Override
	public void encode(BsonWriter bsonWriter, Quantity unit, EncoderContext encoderContext) {
		try {
			bsonWriter.writeString(OBJECT_MAPPER.writeValueAsString(unit));
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to encode unit field.", e);
		}
	}
	
	@Override
	public Class<Quantity> getEncoderClass() {
		return Quantity.class;
	}
}
