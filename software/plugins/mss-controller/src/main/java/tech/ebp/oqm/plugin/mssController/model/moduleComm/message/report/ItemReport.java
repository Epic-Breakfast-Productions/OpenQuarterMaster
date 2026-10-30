package tech.ebp.oqm.plugin.mssController.model.moduleComm.message.report;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ItemReport {

	@NotNull
	@Min(1)
	private int blockNum;

	@NonNull
	@NotNull
	private Operation operation;

	@NonNull
	@NotNull
	private String identifier;

	public enum Operation {
		ADD,
		REMOVE
	}
}
