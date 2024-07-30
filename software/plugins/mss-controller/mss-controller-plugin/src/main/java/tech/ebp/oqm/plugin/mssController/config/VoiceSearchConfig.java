package tech.ebp.oqm.plugin.mssController.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.net.URI;

@ConfigMapping(prefix = "voiceSearch")
public interface VoiceSearchConfig {
	
	@WithName("container")
	ContainerConfig container();
	
	@WithName("enabled")
	boolean enabled();
	
	interface ContainerConfig {
		@WithName("engineUri")
		@WithDefault("unix:///var/run/docker.sock")
		URI engineUri();
		
		@WithName("image")
		String image();
		
		@WithName("tag")
		String tag();
		
		@WithName("volumeLoc")
		@WithDefault("/tmp/oqm/mss-controller-plugin/voice2jsonHome/")
		String volumeLoc();
		
		default String getFullImageRef(){
			return this.image() + ":" + this.tag();
		}
	}
}
