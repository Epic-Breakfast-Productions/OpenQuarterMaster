package com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils;

import com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils.codecs.UnitCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import javax.inject.Inject;
import javax.measure.Unit;

public class CustomCodecProvider implements CodecProvider {

    @Inject
    UnitCodec unitCodec;


    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == Unit.class) {
            return (Codec<T>) new UnitCodec();
        }
        return null;
    }
}
