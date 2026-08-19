package tech.ebp.oqm.plugin.mssController.service.db;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleRecord.ModuleRecord;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;

@ApplicationScoped
public class ModuleRecordRepository implements PanacheRepositoryBase<ModuleRecord, String> {

	public void ensurePresent(String serialId) {
		if (this.findByIdOptional(serialId).isPresent()) {
			return;
		}

		this.persist(
			ModuleRecord.builder()
				.serialId(serialId)
				.build()
		);
	}

	public void ensurePresent(MssConnector connector) {
		this.ensurePresent(connector.getModuleInfo().getSerialId());
	}
}
