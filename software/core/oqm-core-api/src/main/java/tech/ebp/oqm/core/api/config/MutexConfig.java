package tech.ebp.oqm.core.api.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;

@ConfigMapping(prefix = "service.mutex", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface MutexConfig {
	
	@WithDefault("${quarkus.uuid}")
	String instanceId();
	
	@WithDefault("10m")
	Duration lockExpireDuration();
	
	AwaitConfig await();
	
	interface AwaitConfig {
		@WithDefault("30s")
		Duration timeout();
		
		@WithDefault("0ms")
		Duration loopPauseMin();
		
		@WithDefault("50ms")
		Duration loopPauseMax();
	}
}
