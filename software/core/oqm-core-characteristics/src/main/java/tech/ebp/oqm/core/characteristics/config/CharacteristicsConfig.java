package tech.ebp.oqm.core.characteristics.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.nio.file.Path;

@ConfigMapping(prefix = "characteristics")
public interface CharacteristicsConfig {
	
	@WithName("fileLocation")
	Path fileLocation();
	
}