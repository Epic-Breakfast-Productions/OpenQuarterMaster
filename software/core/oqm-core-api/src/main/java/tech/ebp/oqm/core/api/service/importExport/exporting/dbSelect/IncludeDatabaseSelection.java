package tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
//@AllArgsConstructor
@SuperBuilder
public class IncludeDatabaseSelection extends ListBasedDatabaseSelection {

    @Override
    public DatabaseSelectionType getDatabaseSelectionType() {
        return DatabaseSelectionType.INCLUDE;
    }
}
