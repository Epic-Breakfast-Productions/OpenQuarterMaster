package com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils.codecs;

import com.ebp.openQuarterMaster.baseStation.data.mongo.items.InventoryItemEntity;
import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import javax.inject.Singleton;
import javax.measure.Unit;

import java.util.Map;

import static com.ebp.openQuarterMaster.lib.core.Utils.OBJECT_MAPPER;

//@Singleton
//public class InventoryItemCodec<S extends Stored> implements Codec<InventoryItem<S>> {
//    private final Class<InventoryItem<S>> encoderClass;
//    private final Codec<InventoryItem<S>> storedCodec;
//
//    public InventoryItemCodec(Class<InventoryItem<S>> encoderClass, Codec<InventoryItem<S>> storedCodec) {
//        this.encoderClass = encoderClass;
//        this.storedCodec = storedCodec;
//    }
//
//
//    @Override
//    public InventoryItem<S> decode(BsonReader bsonReader, DecoderContext decoderContext) {
//        if()
//    }
//
//    @Override
//    public void encode(BsonWriter bsonWriter, InventoryItem<S> unit, EncoderContext encoderContext) {
//
//    }
//
//}
