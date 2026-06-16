package tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommandResponse {
	@NonNull
	@NotNull
	private CommandResponseType status;

	private String description;

	private ObjectNode response;
}
