package com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils;

import com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils.codecs.AnyMapCodec;
import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.InventoryItemAmt;
import com.ebp.openQuarterMaster.lib.core.InventoryItemTracked;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.Map;

public class InventoryItemCodecProvider implements CodecProvider {
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if(InventoryItemAmt.class.isAssignableFrom(clazz)){
            return (Codec<T>) registry.get(InventoryItemAmt.class);
        }
        if(InventoryItemTracked.class.isAssignableFrom(clazz)){
            return (Codec<T>) registry.get(InventoryItemTracked.class);
        }
        return null;
    }

//    @Override
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    public <T> Codec<T> get(final TypeWithTypeParameters<T> type, final PropertyCodecRegistry registry) {
//        if (InventoryItemAmt.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 1) {
//            return registry.get(InventoryItemAmt.class.getTypeParameters());
//
//            registry.get();
////            return new AnyMapCodec(type.getType(), registry.get(type.getTypeParameters().get(0)), registry.get(type.getTypeParameters().get(1)));
//        }
//        return null;
//    }
}
