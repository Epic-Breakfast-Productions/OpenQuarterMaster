package tech.ebp.oqm.plugin.mssController.model.moduleComm;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.ConnState;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MssConnectorInfo {
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
	private String manufactureDate;

	@NotNull
	@Min(1)
	private int numBlocks;

	@NotNull
	@NonNull
	@Builder.Default
	private Capabilities capabilities = new Capabilities();

	@NonNull
	@NotNull
	@Getter
	private ZonedDateTime lastComm;

	@NonNull
	@NotNull
	@Getter
	@Setter(AccessLevel.PRIVATE)
	private int numErrsSinceLastComm;

	@NonNull
	@NotNull
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private ConnState connState;
}
