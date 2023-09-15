package com.ebp.openQuarterMaster.plugin.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "voiceSearch")
public interface VoiceSearchConfig {
	
	@WithName("container")
	ContainerConfig container();
	
	interface ContainerConfig {
		@WithName("image")
		String image();
		
		@WithName("tag")
		String tag();
		
		@WithName("volumeLoc")
		String volumeLoc();
	}
}
