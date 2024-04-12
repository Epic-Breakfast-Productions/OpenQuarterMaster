package tech.ebp.oqm.core.api.service.importExport.export.dbSelect;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SelectNoDatabases extends DatabaseSelection {
    @Override
    public DatabaseSelectionType getDatabaseSelectionType() {
        return DatabaseSelectionType.NONE;
    }

    @Override
    public boolean isSelected(String databaseIdOrName) {
        return false;
    }
}
