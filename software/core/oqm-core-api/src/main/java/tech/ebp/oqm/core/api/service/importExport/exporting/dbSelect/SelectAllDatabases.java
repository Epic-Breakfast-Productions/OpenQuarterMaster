package tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SelectAllDatabases extends DatabaseSelection {
    @Override
    public DatabaseSelectionType getDatabaseSelectionType() {
        return DatabaseSelectionType.ALL;
    }

    @Override
    public boolean isSelected(String databaseIdOrName) {
        return true;
    }
}
