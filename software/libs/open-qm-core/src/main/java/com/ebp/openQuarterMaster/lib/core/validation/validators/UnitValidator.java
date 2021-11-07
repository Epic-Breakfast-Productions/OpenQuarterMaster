package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;

import javax.measure.Unit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class UnitValidator extends Validator implements ConstraintValidator<ValidUnit, Unit> {

    @Override
    public boolean isValid(Unit unit, ConstraintValidatorContext constraintValidatorContext) {
        List<String> errs = new ArrayList<>();

        if(unit == null){
            return true;
        } else {
            if(!UnitUtils.ALLOWED_UNITS.contains(unit)){
                errs.add("Unit not valid. " + unit.getName() + " not applicable for item storage.");
            }
        }

        return this.processValidationResults(errs, constraintValidatorContext);
    }
}
