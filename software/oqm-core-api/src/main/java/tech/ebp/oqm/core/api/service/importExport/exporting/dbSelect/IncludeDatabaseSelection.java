package tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
//@AllArgsConstructor
@Builder
public class IncludeDatabaseSelection extends ListBasedDatabaseSelection {

    @Override
    public DatabaseSelectionType getDatabaseSelectionType() {
        return DatabaseSelectionType.INCLUDE;
    }
}
