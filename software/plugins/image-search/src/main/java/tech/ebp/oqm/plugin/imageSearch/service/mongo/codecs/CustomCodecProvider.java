package tech.ebp.oqm.plugin.imageSearch.service.mongo.codecs;


import jakarta.enterprise.context.ApplicationScoped;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;

@ApplicationScoped
public class CustomCodecProvider implements CodecProvider {
	
	private final List<Codec<?>> codecs = List.of(
		new FloatArrayCodec()
	);
	
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		//noinspection rawtypes
		for (Codec codec : this.codecs) {
			if (clazz == codec.getEncoderClass()) {
				//noinspection unchecked
				return (Codec<T>) codec;
			}
		}
		return null;
	}
	
	
}
