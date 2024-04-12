package tech.ebp.oqm.core.api.service.importExport.export.dbSelect;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public abstract class ListBasedDatabaseSelection extends DatabaseSelection {
    private boolean useRegexMatch;
    private Set<String> list = new HashSet<>();

    @Override
    public boolean isSelected(String databaseIdOrName) {
        boolean contains = this.list.contains(databaseIdOrName);

        return switch (this.getDatabaseSelectionType()){
            case INCLUDE -> contains;
            case EXCLUDE -> !contains;
            case ALL, NONE -> throw new IllegalStateException("This should not extend these.");
        };
    }
}
