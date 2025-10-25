package tech.ebp.oqm.core.api.service.mongo.utils;


import jakarta.enterprise.context.ApplicationScoped;
import org.bson.codecs.Codec;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.BigIntCodec;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.ColorCodec;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.DurationCodec;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.QuantityCodec;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.UUIDCodec;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.UnitCodec;
import tech.ebp.oqm.core.api.service.mongo.utils.codecs.ZonedDateTimeCodec;

import java.util.List;

@ApplicationScoped
public class CustomCodecProvider implements CodecProvider {
	
	@SuppressWarnings("deprecation")
	List<Codec<?>> codecs = List.of(
		new UUIDCodec(),
		new QuantityCodec(),
		new UnitCodec(),
		new ColorCodec(),
		new ZonedDateTimeCodec(),
		new DurationCodec(),
		new BigIntCodec()
//		new MapCodec()//deprecated
	);
	
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		//noinspection rawtypes
		for (Codec codec : codecs) {
			if (clazz == codec.getEncoderClass()) {
				//noinspection unchecked
				return (Codec<T>) codec;
			}
		}
		return null;
	}
	
	
}
