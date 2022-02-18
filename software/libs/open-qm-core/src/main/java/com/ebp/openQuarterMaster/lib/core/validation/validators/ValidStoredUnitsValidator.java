package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.items.AmountItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidHeldStoredUnits;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;


public class ValidStoredUnitsValidator extends Validator<ValidHeldStoredUnits, AmountItem> {
	
	public static final String INVALID_UNITS_FOUND_FORMAI = "Found %d stored objects with units incompatible with the item's.";
	
	@Override
	public boolean isValid(AmountItem item, ConstraintValidatorContext constraintValidatorContext) {
		List<String> validationErrs = new ArrayList<>();
		
		long invalidCount = item
			.getStorageMap()
			.values()
			.stream()
			.flatMap(List::stream)
			.filter(
				(AmountStored curStored)->{
					return !item.getUnit().isCompatible(curStored.getAmount().getUnit());
				}
			).count();
		if (invalidCount > 0) {
			validationErrs.add(String.format(INVALID_UNITS_FOUND_FORMAI, invalidCount));
		}
		
		return this.processValidationResults(validationErrs, constraintValidatorContext);
	}
}
