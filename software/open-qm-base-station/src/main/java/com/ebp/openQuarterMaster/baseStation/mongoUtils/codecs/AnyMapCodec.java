package com.ebp.openQuarterMaster.baseStation.mongoUtils.codecs;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;


public class AnyMapCodec<K, T> implements Codec<Map<K, T>> {
	
	private final Class<K> keyClass;
	private final Class<Map<K, T>> encoderClass;
	private final Codec<K> keyCodec;
	private final Codec<T> valueCodec;
	
	public AnyMapCodec(Class<K> keyClass, Class<Map<K, T>> encoderClass, Codec<K> keyCodec, Codec<T> valueCodec) {
		this.keyClass = keyClass;
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
				
				String name;
				if(keyClass.isAssignableFrom(ObjectId.class)){
					name = ((ObjectId)entry.getKey()).toHexString();
				} else {
					name = entry.getKey().toString();
				}
				writer.writeName(name);
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
			String keyString = reader.readName();
			T value = valueCodec.decode(reader, context);
			K key;
			
			if(this.keyClass.isAssignableFrom(ObjectId.class)){
				key = (K) new ObjectId(keyString);
			} else if(this.keyClass.isAssignableFrom(String.class)){
				key = (K) keyString;
			} else {
				throw new IllegalArgumentException("Cannot decode map with key type " + this.keyClass.getName());
			}
			
			map.put(key, value);
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
		} catch(final Exception e) {
			throw new CodecConfigurationException(e.getMessage(), e);
		}
	}
}
