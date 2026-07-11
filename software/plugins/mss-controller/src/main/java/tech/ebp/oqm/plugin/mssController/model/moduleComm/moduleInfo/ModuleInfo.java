package tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleInfo {

	@NonNull
	@NotNull
	private String specVersion;

	@NonNull
	@NotNull
	private String firmwareVersion;

	@NonNull
	@NotNull
	private String serialId;

	@NonNull
	@NotNull
	private LocalDate manufactureDate;

	@NotNull
	@Min(1)
	private int numBlocks;

	@NotNull
	@NonNull
	@Builder.Default
	private Capabilities capabilities = new Capabilities();
}
