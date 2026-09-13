package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.validation.annotations.NonZeroQuantity;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidQuantity;

import javax.measure.Quantity;
import java.util.ArrayList;
import java.util.List;

public class NonZeroQuantityValidator extends Validator<NonZeroQuantity, Quantity> {

	@Override
	public boolean isValid(Quantity quantity, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();

		if (quantity == null) {
			return true;
		} else {
			if(quantity.getValue().doubleValue() <= 0){
				errs.add("Invalid quantity given. The value must be greater than zero.");
			}
		}

		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
