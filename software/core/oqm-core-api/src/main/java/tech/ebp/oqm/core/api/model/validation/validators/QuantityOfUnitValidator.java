package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidQuantityOfUnit;

import javax.measure.Quantity;
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
