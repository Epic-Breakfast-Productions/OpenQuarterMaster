package tech.ebp.oqm.baseStation.rest.dataImportExport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
	
	@lombok.Builder.Default
	private long numUnits = 0;
	@lombok.Builder.Default
	private long numImages = 0;
	@lombok.Builder.Default
	private long numFileAttachments = 0;
	@lombok.Builder.Default
	private long numItemCategories = 0;
	@lombok.Builder.Default
	private long numStorageBlocks = 0;
	@lombok.Builder.Default
	private long numInventoryItems = 0;
	@lombok.Builder.Default
	private long numItemLists = 0;
}
