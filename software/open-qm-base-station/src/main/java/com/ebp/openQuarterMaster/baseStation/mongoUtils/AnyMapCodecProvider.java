package com.ebp.openQuarterMaster.baseStation.mongoUtils;

import com.ebp.openQuarterMaster.baseStation.mongoUtils.codecs.AnyMapCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.Map;

public class AnyMapCodecProvider implements PropertyCodecProvider, CodecProvider {
	
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> Codec<T> get(final TypeWithTypeParameters<T> type, final PropertyCodecRegistry registry) {
		if (Map.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 2) {
			return new AnyMapCodec(
				type.getType(),
				registry.get(type.getTypeParameters().get(0)),
				registry.get(type.getTypeParameters().get(1))
			);
		}
		return null;
	}
	
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (Map.class.isAssignableFrom(clazz) && clazz.getTypeParameters().length == 2) {
			return new AnyMapCodec(
				clazz,
				registry.get(clazz.getTypeParameters()[0].getGenericDeclaration()),
				registry.get(clazz.getTypeParameters()[1].getGenericDeclaration())
			);
		}
		return null;
	}
}
