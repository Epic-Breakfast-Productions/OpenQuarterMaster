package tech.ebp.oqm.lib.core.rest.unit;

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
