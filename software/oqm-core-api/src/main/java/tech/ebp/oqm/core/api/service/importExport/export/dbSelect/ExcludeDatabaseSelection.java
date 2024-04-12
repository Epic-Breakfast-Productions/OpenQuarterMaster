package tech.ebp.oqm.core.api.service.importExport.export.dbSelect;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcludeDatabaseSelection extends ListBasedDatabaseSelection {

    @Override
    public DatabaseSelectionType getDatabaseSelectionType() {
        return DatabaseSelectionType.EXCLUDE;
    }
}
