package tech.ebp.oqm.core.api.service.importExport.exporting;

import lombok.*;
import tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect.DatabaseSelection;
import tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect.SelectAllDatabases;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExportOptions {

    @lombok.Builder.Default
    private Boolean includeHistory = true;

    @lombok.Builder.Default
    private DatabaseSelection databaseSelection = new SelectAllDatabases();
}
