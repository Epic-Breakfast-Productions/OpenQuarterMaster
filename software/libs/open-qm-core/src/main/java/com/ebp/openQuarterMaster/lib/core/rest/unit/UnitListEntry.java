package com.ebp.openQuarterMaster.lib.core.rest.unit;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
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

    public UnitListEntry(Unit unit){
        this(unit, unit.toString());
    }
}
