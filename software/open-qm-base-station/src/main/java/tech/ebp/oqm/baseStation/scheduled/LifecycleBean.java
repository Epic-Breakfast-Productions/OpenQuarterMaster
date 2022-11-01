package tech.ebp.oqm.baseStation.scheduled;

import com.mongodb.client.model.Sorts;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.lib.core.units.UnitUtils;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;

@Singleton
@Slf4j
public class LifecycleBean {
	
	@Inject
	@Location("other/StartBannerTemplate")
	Template startTemplate;
	
	@Inject
	CustomUnitService customUnitService;
	
	private ZonedDateTime startDateTime;
	
	void onStart(
		@Observes
		StartupEvent ev
	) {
		this.startDateTime = ZonedDateTime.now();
		log.info("Open QuarterMaster Web Server starting.");
		//		log.info("Base URL: {}", this.serverUrlService.getBaseServerUrl());
		//		log.debug("Version: {}", this.serverVersion);
		//		log.debug("build time: {}", this.buildTime);
		//		log.debug("Core lib version: {}", this.coreVersion);
		//		log.debug("ManagerIO lib version: {}", this.managerIOVersion);
		//		log.debug("Stats lib version: {}", this.statsVersion);
		//		log.debug("Web lib version: {}", this.webLibVersion);
		
		log.info(this.startTemplate.render());
		
		if (log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (String curProp : ConfigProvider.getConfig().getPropertyNames()) {
				String value;
				try {
					value = ConfigProvider.getConfig().getValue(curProp, String.class);
				} catch(NoSuchElementException e) {
					value = "";
				}
				sb.append('\t');
				sb.append(curProp);
				sb.append('=');
				sb.append(value);
				sb.append(System.lineSeparator());
			}
			log.debug("Configuration: \n{}", sb);
		}
		
		log.info("Reading existing custom units from database...");
		UnitUtils.registerAllUnits(
			this.customUnitService.list(null, Sorts.ascending("order"), null)
		);
		log.info("Done.");
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
