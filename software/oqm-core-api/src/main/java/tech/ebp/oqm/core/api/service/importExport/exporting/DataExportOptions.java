package tech.ebp.oqm.core.api.service.importExport.exporting;

import lombok.*;
import tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect.DatabaseSelection;
import tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect.SelectAllDatabases;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExportOptions {
    private Boolean includeHistory = true;
    private DatabaseSelection databaseSelection = new SelectAllDatabases();
}
