package tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.CommandType;

//@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(callSuper = true)
public class HighlightBlockSetting {

	@NotNull
	@Min(1)
	private int blockNum;

	private HighlightBlockPowerSetting lightPowerState = HighlightBlockPowerSetting.ON;

	private String lightColor;

	@Min(0)
	@Max(255)
	private int brightness;
}
