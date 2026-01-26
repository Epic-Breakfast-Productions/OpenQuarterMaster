package tech.ebp.oqm.core.characteristics.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.nio.file.Path;

@ConfigMapping(prefix = "uis")
public interface UiConfig {
	
	//TODO:: this
	
	@WithName("fileLocation")
	Path fileLocation();
	
}