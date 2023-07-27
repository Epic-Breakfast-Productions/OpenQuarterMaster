package tech.ebp.oqm.baseStation.model.validation.validators;

import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidQuantity;
import tech.ebp.oqm.baseStation.model.validation.validators.Validator;

import javax.measure.Quantity;
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
			if (!UnitUtils.UNIT_LIST.contains(quantity.getUnit())) {
				errs.add("Invalid unit. \"" + quantity.getUnit().toString() + "\" not applicable for item storage.");
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
