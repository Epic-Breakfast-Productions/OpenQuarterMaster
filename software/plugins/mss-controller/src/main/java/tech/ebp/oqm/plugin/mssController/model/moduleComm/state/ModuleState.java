package tech.ebp.oqm.plugin.mssController.model.moduleComm.state;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleState {
	@NonNull
	@NotNull
	private List<BlockState> storageBlocks;
}
