package tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class ListBasedDatabaseSelection extends DatabaseSelection {
	
	@lombok.Builder.Default
	private boolean useRegexMatch = false;
	
	@lombok.Builder.Default
	private Set<String> list = new HashSet<>();
	
	@Override
	public boolean isSelected(String databaseIdOrName) {
		boolean contains = this.list.contains(databaseIdOrName);
		
		return switch (this.getDatabaseSelectionType()) {
			case INCLUDE -> contains;
			case EXCLUDE -> !contains;
			case ALL, NONE -> throw new IllegalStateException("This should not extend these.");
		};
	}
}
