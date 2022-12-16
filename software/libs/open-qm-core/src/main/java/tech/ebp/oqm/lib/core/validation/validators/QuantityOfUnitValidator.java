package tech.ebp.oqm.lib.core.validation.validators;

import tech.ebp.oqm.lib.core.units.OqmProvidedUnits;
import tech.ebp.oqm.lib.core.validation.annotations.ValidQuantity;
import tech.ebp.oqm.lib.core.validation.annotations.ValidQuantityOfUnit;

import javax.measure.Quantity;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class QuantityOfUnitValidator extends Validator<ValidQuantityOfUnit, Quantity> {
	
	@Override
	public boolean isValid(Quantity quantity, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (quantity == null) {
			return true;
		} else {
			if (!OqmProvidedUnits.UNIT.equals(quantity.getUnit())) {
				errs.add("Must be a quantity of Units.");
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
