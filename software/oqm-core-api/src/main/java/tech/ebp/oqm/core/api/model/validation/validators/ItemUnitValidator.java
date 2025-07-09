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
		
		switch (item.getStorageType().storedType){
			case AMOUNT -> {
				//nothing to do
			}
			case UNIQUE -> {
				if(!OqmProvidedUnits.UNIT.isCompatible(item.getUnit())){
					errs.add("Items using unique type must use unit that is compatible with the unit 'unit'.");
				}
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
