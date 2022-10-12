package tech.ebp.oqm.baseStation.service.mongo.utils;


import org.bson.codecs.Codec;
import org.bson.codecs.MapCodec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import tech.ebp.oqm.baseStation.service.mongo.utils.codecs.DurationCodec;
import tech.ebp.oqm.baseStation.service.mongo.utils.codecs.QuantityCodec;
import tech.ebp.oqm.baseStation.service.mongo.utils.codecs.UUIDCodec;
import tech.ebp.oqm.baseStation.service.mongo.utils.codecs.UnitCodec;
import tech.ebp.oqm.baseStation.service.mongo.utils.codecs.ZonedDateTimeCodec;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CustomCodecProvider implements CodecProvider {
	
	List<Codec<?>> codecs = List.of(
		new UUIDCodec(),
		new QuantityCodec(),
		new UnitCodec(),
		new ZonedDateTimeCodec(),
		new DurationCodec(),
		new MapCodec()
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
