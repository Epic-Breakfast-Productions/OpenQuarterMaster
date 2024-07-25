package tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandResponse {
	
	private CommandResponseStatus status;
	private String details = null;
}
