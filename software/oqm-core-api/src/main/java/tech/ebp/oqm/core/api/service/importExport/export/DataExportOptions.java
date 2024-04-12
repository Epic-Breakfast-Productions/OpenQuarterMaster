package tech.ebp.oqm.core.api.service.importExport.export;

import lombok.*;
import tech.ebp.oqm.core.api.service.importExport.export.dbSelect.DatabaseSelection;
import tech.ebp.oqm.core.api.service.importExport.export.dbSelect.SelectAllDatabases;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExportOptions {
    private Boolean includeHistory = true;
    private DatabaseSelection databaseSelection = new SelectAllDatabases();
}
