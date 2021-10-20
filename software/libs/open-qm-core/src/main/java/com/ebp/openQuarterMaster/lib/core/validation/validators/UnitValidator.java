package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;

import javax.measure.Unit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UnitValidator implements ConstraintValidator<ValidUnit, Unit> {

    @Override
    public boolean isValid(Unit unit, ConstraintValidatorContext constraintValidatorContext) {
        return Utils.ALLOWED_MEASUREMENTS.contains(unit);
    }
}
