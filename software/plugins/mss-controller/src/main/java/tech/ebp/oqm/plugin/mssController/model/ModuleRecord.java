package tech.ebp.oqm.plugin.mssController.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.ConnectionType;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleRecord {

	private String serialId;

	private ConnectionType connectionType;

	@Builder.Default
	private ZonedDateTime lastSeen = ZonedDateTime.now();
}
