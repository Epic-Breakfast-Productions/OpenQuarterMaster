package tech.ebp.oqm.baseStation.model.validation.validators;

import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidUnit;
import tech.ebp.oqm.baseStation.model.validation.validators.Validator;

import javax.measure.Unit;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class UnitValidator extends Validator<ValidUnit, Unit> {
	
	@Override
	public boolean isValid(Unit unit, ConstraintValidatorContext constraintValidatorContext) {
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
