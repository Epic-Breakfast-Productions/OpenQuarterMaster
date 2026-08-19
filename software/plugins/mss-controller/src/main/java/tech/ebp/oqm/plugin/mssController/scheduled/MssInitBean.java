package tech.ebp.oqm.plugin.mssController.scheduled;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Priorities;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.service.mssConn.MssConnectionService;

@Singleton
@Slf4j
public class MssInitBean {

	@Inject
	MssConnectionService mssConnectionService;

	@Transactional
	void onStartUp(
		@Observes
		StartupEvent ev
	) {
		this.mssConnectionService.initializeMssConnections();
	}
}
