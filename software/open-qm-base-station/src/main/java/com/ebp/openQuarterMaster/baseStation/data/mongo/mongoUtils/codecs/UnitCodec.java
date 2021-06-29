package com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import javax.inject.Singleton;
import javax.measure.Unit;

@Singleton
public class UnitCodec implements Codec<Unit> {

    @Override
    public Unit decode(BsonReader bsonReader, DecoderContext decoderContext) {
        String unitData = bsonReader.readString();

        //todo

        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, Unit unit, EncoderContext encoderContext) {
        System.out.println("Encoding....");
//
//        bsonWriter.writeDateTime(Date.from(status.getTimestamp().toInstant(ZoneOffset.UTC)).getTime());
//        bsonWriter.writeString(status.name());

        //TODO
    }

    @Override
    public Class<Unit> getEncoderClass() {
        return Unit.class;
    }
}
