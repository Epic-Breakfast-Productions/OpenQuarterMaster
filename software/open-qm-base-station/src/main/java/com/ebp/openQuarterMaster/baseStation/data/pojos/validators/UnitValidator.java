package com.ebp.openQuarterMaster.baseStation.data.pojos.validators;

import javax.measure.Unit;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

import static com.ebp.openQuarterMaster.baseStation.data.pojos.Utils.ALLOWED_MEASUREMENTS;

public class UnitValidator implements ConstraintValidator<ValidUnit, Unit> {

    @Override
    public boolean isValid(Unit unit, ConstraintValidatorContext constraintValidatorContext) {
        return ALLOWED_MEASUREMENTS.contains(unit);
    }
}
