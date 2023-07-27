package tech.ebp.oqm.baseStation.model.validation.validators;

import tech.ebp.oqm.baseStation.model.units.OqmProvidedUnits;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidQuantityOfUnit;
import tech.ebp.oqm.baseStation.model.validation.validators.Validator;

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
