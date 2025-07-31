package tech.ebp.oqm.core.api.model.rest.unit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.measure.Unit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitListEntry {
	
	Unit unit;
	String displayName = "";
	
	public UnitListEntry(Unit unit) {
		this(unit, unit.toString());
	}
}
