package com.ebp.openQuarterMaster.baseStation.mongoUtils.codecs;

import com.ebp.openQuarterMaster.lib.core.test.TestSuper;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class TestSuperDecoder implements Codec<TestSuper> {
	
	private final CodecRegistry registry;
	
	public TestSuperDecoder(CodecRegistry registry) {
		this.registry = registry;
	}
	
	
	@Override
	public TestSuper decode(BsonReader reader, DecoderContext decoderContext) {
		
		return null;
	}
	
	@Override
	public void encode(BsonWriter writer, TestSuper value, EncoderContext encoderContext) {
		//        this.registry.get(value.getClass())
		//                .encode(
		//                        writer,
		//                        (value.getClass()).cast(value),
		//                        encoderContext
		//                );
	}
	
	@Override
	public Class<TestSuper> getEncoderClass() {
		return TestSuper.class;
	}
}
