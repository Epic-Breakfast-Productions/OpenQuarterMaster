package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidInventoryItem;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InventoryItemValidator implements ConstraintValidator<ValidInventoryItem, InventoryItem> {

    @Override
    public boolean isValid(InventoryItem unit, ConstraintValidatorContext constraintValidatorContext) {
        //TODO
        return true;
    }
}
