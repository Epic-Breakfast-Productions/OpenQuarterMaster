package com.ebp.openQuarterMaster.plugin;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.ModuleMaster;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Singleton
@Slf4j
public class LifecycleBean {
	
	@Inject
	ModuleMaster moduleMaster;
	
//	@Inject
//	VoiceSearchService voiceSearchService;
	
	void onStart(
		@Observes
		StartupEvent ev
	) throws IOException {
		log.info(
			"Finished initting Module Master. Picked up on modules: {}",
			moduleMaster.getModuleIds()
		);
		
//		if(this.voiceSearchService.enabled()) {
//			log.info(
//				"Speech Search using image: {}",
//				voiceSearchService.getCurImageInformation()
//			);
//		}
	}
	
	void onStop(
		@Observes
		ShutdownEvent ev
	) {
	
	}
}
