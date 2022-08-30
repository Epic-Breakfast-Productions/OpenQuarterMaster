package tech.ebp.oqm.lib.core.validation.validators;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;

import javax.measure.Unit;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class UnitValidator extends Validator<ValidUnit, Unit> {
	
	@Override
	public boolean isValid(Unit unit, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (unit == null) {
			return true;
		} else {
			if (!UnitUtils.ALLOWED_UNITS.contains(unit)) {
				errs.add("Invalid unit. " + unit.toString() + " not applicable for item storage.");
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
