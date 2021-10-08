package com.ebp.openQuarterMaster.baseStation.data.mongo.mongoUtils.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.json.JsonReader;

import javax.measure.Unit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AnyMapCodec<K, T> implements Codec<Map<K, T>> {
    private final Class<Map<K, T>> encoderClass;
    private final Codec<K> keyCodec;
    private final Codec<T> valueCodec;

    public AnyMapCodec(Class<Map<K, T>> encoderClass, Codec<K> keyCodec, Codec<T> valueCodec) {
        this.encoderClass = encoderClass;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    @Override
    public void encode(final BsonWriter writer, final Map<K, T> map, final EncoderContext encoderContext) {
        try (var dummyWriter = new BsonDocumentWriter(new BsonDocument())) {
            dummyWriter.writeStartDocument();
            writer.writeStartDocument();
            for (final Map.Entry<K, T> entry : map.entrySet()) {
                var dummyId = UUID.randomUUID().toString();
                dummyWriter.writeName(dummyId);
                keyCodec.encode(dummyWriter, entry.getKey(), encoderContext);
                //TODO: could it be simpler by something like JsonWriter?
                writer.writeName(dummyWriter.getDocument().asDocument().get(dummyId).asString().getValue());
                valueCodec.encode(writer, entry.getValue(), encoderContext);
            }
            dummyWriter.writeEndDocument();
        }
        writer.writeEndDocument();
    }

    @Override
    public Map<K, T> decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartDocument();
        Map<K, T> map = getInstance();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            //TODO: what if the key is not a String aka not wrapped in double quotes?
            var nameReader = new JsonReader("{\"key:\":\"" + reader.readName() + "\"}");
            nameReader.readStartDocument();
            nameReader.readBsonType();
            if (reader.getCurrentBsonType() == BsonType.NULL) {
                map.put(keyCodec.decode(nameReader, context), null);
                reader.readNull();
            } else {
                map.put(keyCodec.decode(nameReader, context), valueCodec.decode(reader, context));
            }
            nameReader.readEndDocument();
        }
        reader.readEndDocument();
        return map;
    }

    @Override
    public Class<Map<K, T>> getEncoderClass() {
        return encoderClass;
    }

    private Map<K, T> getInstance() {
        if (encoderClass.isInterface()) {
            return new HashMap<>();
        }
        try {
            return encoderClass.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            throw new CodecConfigurationException(e.getMessage(), e);
        }
    }
}
