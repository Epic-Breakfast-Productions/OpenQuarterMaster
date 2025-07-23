package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidItemUnit;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUnit;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.List;

public class ItemUnitValidator extends Validator<ValidItemUnit, InventoryItem> {
	
	@Override
	public boolean isValid(InventoryItem item, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		Unit<?> unit = item.getUnit();
		switch (item.getStorageType().storedType){
			case AMOUNT -> {
				//this might be redundant (we can't deserialize a unit not in this list), but can't hurt
				if(!UnitUtils.UNIT_LIST.contains(unit)){
					errs.add("Invalid unit: " + unit.toString() + " not applicable for item storage. Must use unit in the unit list.");
				}
			}
			case UNIQUE -> {
				if(!OqmProvidedUnits.UNIT.isCompatible(unit)){
					errs.add("Items using unique type must use unit that is compatible with the unit 'unit'.");
				}
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
