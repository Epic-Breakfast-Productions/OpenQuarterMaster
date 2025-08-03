package tech.ebp.oqm.core.api.scheduled;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.object.upgrade.TotalUpgradeResult;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;
import tech.ebp.oqm.core.api.service.schemaVersioning.ObjectSchemaUpgradeService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

@Singleton
@Slf4j
public class LifecycleBean {
	
	@ConfigProperty(name="service.version")
	String serviceVersion;
	
	@ConfigProperty(name="service.apiVersion")
	String apiVersion;
	
	@Inject
	CustomUnitService customUnitService;
	
	@Inject
	TempFileService tempFileService;
	
	@Inject
	OqmDatabaseService dbService;

	@Inject
	ObjectSchemaUpgradeService objectSchemaUpgradeService;
	
	private ZonedDateTime startDateTime;
	
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
	
	private void startLogAnnounce(){
		this.startDateTime = ZonedDateTime.now();
		log.info("Open QuarterMaster Core API Server starting.");
		//		log.info("Base URL: {}", this.serverUrlService.getBaseServerUrl());
		//		log.debug("Version: {}", this.serverVersion);
		//		log.debug("build time: {}", this.buildTime);
		//		log.debug("Core lib version: {}", this.coreVersion);
		//		log.debug("ManagerIO lib version: {}", this.managerIOVersion);
		//		log.debug("Stats lib version: {}", this.statsVersion);
		//		log.debug("Web lib version: {}", this.webLibVersion);
		
		if(log.isInfoEnabled()) {
			// Image: https://www.text-image.com/convert/ascii.html
			// Text: https://manytools.org/hacker-tools/ascii-banner/ (Colossal font)
			log.info("""
            &&&&
        &&&&&&&&&&&
     &&&&&&&    &&&&&&&
 &&&&&&&           &&&&&&&&            &&&
&&&&&                  &&&&&        &&&&&&&&&&           .d8888b.
&&&&                    &&&&    &&&&&&&&&&&&&&&&&       d88P  Y88b
&&&&                    &&&&   &&&&&&&&&&&&&&&&&&&      888    888
&&&&                    &&&&   &&&&&&&&&&&&&&&&&&&      888         .d88b.  888d888 .d88b.
&&&&                    &&&&   &&&&&&&&&&&&&&&&&&&      888        d88""88b 888P"  d8P  Y8b
&&&&                    &&&&   &&&&&&&&&&&&&&&&&&&      888    888 888  888 888    88888888
&&&&&                  &&&&&    &&&&&&&&&&&&&&&&&&      Y88b  d88P Y88..88P 888    Y8b.
 &&&&&&&&          &&&&&&&         &&&&&&&&&&&&          "Y8888P"   "Y88P"  888     "Y8888
     &&&&&&&&  &&&&&&&&     &BPB&     &&&&&
         &&&&&&&&&&     &#PY?7!7?YG#
            &&&      #GY?7!7777777!7?5#
     PJPB&       &B5J7!77777777777777!5                        d8888 8888888b. 8888888
     P!!7?YP#&BPJ77!77777777777777777!5                       d88888 888   Y88b  888
     P!7777!777!777777777777777777777!5                      d88P888 888    888  888
     P!777777777777777777777777777777!5                     d88P 888 888   d88P  888
     P!777777777777777777777777777777!5                    d88P  888 8888888P"   888
     P!777777777777777777777777777777!5                   d88P   888 888         888
     P!777777777777777777777777777777!5                  d8888888888 888         888
     P!777777777777777777777777777777!5                 d88P     888 888       8888888
     BJ77!777777777777777777777777!!7?G
       &G5?7!777777777777777777!7?YG#
           #GY?7!7777777777!7?YP#
              &BPJ?7777777JPB&
                  &B5??YG&

Version:     {}
API Version: {}
""",
				this.serviceVersion,
				this.apiVersion
			);
		}
		
		logConfig();

		log.info("Starting in directory: {}", Paths.get("").toAbsolutePath());
	}
	
	void onStart(
		@Observes
		StartupEvent ev
	) {
		this.startLogAnnounce();
		//ensures the db service bean is initialized, and the extension has had time to init
		this.dbService.collectionStats();
		//ensures the unit service bean is initialized, and the extension had existing custom units read in
		this.customUnitService.collectionStats();
		//ensures we can write to temp dir
		this.tempFileService.getTempDir("test", "dir");
		// Upgrade the db schema
		//TODO:: mutex lock on this, wait until done upgrading
		//TODO:: create flag service to check if things initted right. Setup filter to check this flag to reject requests until setup done.
		Optional<TotalUpgradeResult> schemaUpgradeResult = this.objectSchemaUpgradeService.updateSchema();
		if(schemaUpgradeResult.isEmpty()){
			log.warn("Did not upgrade schema at start.");
		} else {
			log.info("Schema upgrade result: {}", schemaUpgradeResult.get());
			//TODO:: rescan inv update stats
		}
		log.info("Done with initial startup tasks.");
	}
	
	void onStop(
		@Observes
		ShutdownEvent ev
	) {
		log.info("The server is stopping.");
		Duration runtime = Duration.between(this.startDateTime, ZonedDateTime.now());
		log.info("Server ran for {}", runtime);
	}
}
