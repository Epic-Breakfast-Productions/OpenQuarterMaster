package tech.ebp.oqm.plugin.mssController;

import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.plugin.mssController.moduleInteraction.ModuleMaster;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.TreeMap;

@Singleton
@Slf4j
public class LifecycleBean {
	
	@Inject
	ModuleMaster moduleMaster;
	
//	@Inject
//	VoiceSearchService voiceSearchService;

	public static void logConfig(){
		if (log.isDebugEnabled()) {
			TreeMap<String, String> configMap = new TreeMap<>();

			for(String curProp : ConfigProvider.getConfig().getPropertyNames()){
				String value;
				try {
					value = ConfigProvider.getConfig().getValue(curProp, String.class);
				} catch(NoSuchElementException e) {
					value = "";
				}
				configMap.put(curProp, value);
			}

			StringBuilder sb = new StringBuilder();
			for (String curProp : configMap.keySet()) {

				sb.append('\t');
				sb.append(curProp);
				sb.append('=');
				sb.append(configMap.get(curProp));
				sb.append(System.lineSeparator());
			}
			log.debug("Configuration: \n{}", sb);
		}
	}
	
	void onStart(
		@Observes
		StartupEvent ev
	) throws IOException {
		logConfig();

		log.info(
			"Finished initting Module Master. Picked up on modules: {}",
			this.moduleMaster.getModuleIds()
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
