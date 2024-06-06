package tech.ebp.oqm.core.api.service.importExport.importing.options;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect.DatabaseSelection;
import tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect.SelectAllDatabases;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataImportOptions {

	@lombok.Builder.Default
	private Boolean includeHistory = true;

	@lombok.Builder.Default
	private DatabaseSelection databaseSelection = new SelectAllDatabases();

	@lombok.Builder.Default
	private DbImportMergeStrategy dbMergeStrategy = DbImportMergeStrategy.MERGE;

	@lombok.Builder.Default
	private Set<InteractingEntityMapStrategy> interactingEntityMapStrategies = new HashSet<>(Arrays.stream(InteractingEntityMapStrategy.values()).toList());
}
