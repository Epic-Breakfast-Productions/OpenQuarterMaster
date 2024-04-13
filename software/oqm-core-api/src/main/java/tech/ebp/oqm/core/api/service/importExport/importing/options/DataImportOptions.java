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
	private Boolean includeHistory = true;
	private DatabaseSelection databaseSelection = new SelectAllDatabases();
	private DbImportMergeStrategy dbMergeStrategy = DbImportMergeStrategy.MERGE;
	private Set<InteractingEntityMapStrategy> interactingEntityMapStrategies = new HashSet<>(Arrays.stream(InteractingEntityMapStrategy.values()).toList());
}
