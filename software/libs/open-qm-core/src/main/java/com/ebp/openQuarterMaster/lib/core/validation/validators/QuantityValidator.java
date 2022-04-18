package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidQuantity;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class QuantityValidator extends Validator<ValidQuantity, Quantity> {
	
	@Override
	public boolean isValid(Quantity quantity, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (quantity == null) {
			return true;
		} else {
			if (!UnitUtils.ALLOWED_UNITS.contains(quantity.getUnit())) {
				errs.add("Invalid unit. \"" + quantity.getUnit().toString() + "\" not applicable for item storage.");
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
