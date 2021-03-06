package com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils;

import com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils.codecs.UUIDCodec;
import com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils.codecs.UnitCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class CustomCodecProvider implements CodecProvider {

    List<Codec> codecs = List.of(
            new UUIDCodec(),
            new UnitCodec()
    );

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        for(Codec codec : codecs){
            if (clazz == codec.getEncoderClass()) {
                return (Codec<T>) codec;
            }
        }
        return null;
    }
}
