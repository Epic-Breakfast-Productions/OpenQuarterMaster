package tech.ebp.oqm.core.api.service.importExport.export.dbSelect;

import lombok.*;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncludeDatabaseSelection extends ListBasedDatabaseSelection {

    @Override
    public DatabaseSelectionType getDatabaseSelectionType() {
        return DatabaseSelectionType.INCLUDE;
    }
}
