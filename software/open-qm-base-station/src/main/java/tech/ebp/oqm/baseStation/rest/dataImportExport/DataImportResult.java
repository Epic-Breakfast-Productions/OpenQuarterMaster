package tech.ebp.oqm.baseStation.rest.dataImportExport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class DataImportResult {

	@Builder.Default
	private long numImages = 0;
	@Builder.Default
	private long numStorageBlocks = 0;
	@Builder.Default
	private long numInventoryItems = 0;
}
