package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUnit;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.List;

public class UnitValidator extends Validator<ValidUnit, Unit<?>> {
	
	@Override
	public boolean isValid(Unit<?> unit, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (unit == null) {
			return true;
		} else {
			if (!UnitUtils.UNIT_LIST.contains(unit)) {
				errs.add("Invalid unit. " + unit.toString() + " not applicable for item storage.");
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
