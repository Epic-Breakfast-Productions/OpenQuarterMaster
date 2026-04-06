package tech.ebp.oqm.core.api.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;

@ConfigMapping(prefix="service.image.resizing", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface ImageResizeConfig {
	@WithDefault("true")
	boolean enabled();
	
	int height();
	int width();
	
	String savedType();
	
	@WithDefault("1")
	Float jsonCompression();
	
	default boolean isJpg(){
		return List.of("jpg", "jpeg").contains(savedType().toLowerCase());
	}
}
