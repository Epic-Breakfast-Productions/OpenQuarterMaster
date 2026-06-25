package tech.ebp.oqm.plugin.mssController.scheduled;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.service.mssConn.MssConnectionService;

@Singleton
@Slf4j
public class MssInitBean {

	@Inject
	MssConnectionService mssConnectionService;


	void onStartUp(
		@Observes
		StartupEvent ev
	) {
		this.mssConnectionService.initializeMssConnections();
	}
}
