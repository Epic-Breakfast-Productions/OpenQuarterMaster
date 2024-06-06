package tech.ebp.oqm.core.api.rest.dataImportExport;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class DbImportResult {
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
