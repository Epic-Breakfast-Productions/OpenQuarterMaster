package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidStored;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.stream.Collectors;


public class StoredValidator implements ConstraintValidator<ValidStored, Stored> {

    private boolean validateAmountStored(Stored stored, ConstraintValidatorContext constraintValidatorContext) {
        return stored.getItems() == null && stored.getAmount() != null;
    }

    private boolean validateTrackedStored(Stored stored, ConstraintValidatorContext constraintValidatorContext) {
        if (stored.getItems() == null) {
            return false;
        }

        if(stored.getItems()
                .values()
                .stream()
                .anyMatch(Objects::isNull)
        ){
            return false;
        }
        return true;
    }

    @Override
    public boolean isValid(Stored stored, ConstraintValidatorContext constraintValidatorContext) {
        if (stored == null) {
            return false;
        }
        if (stored.getType() == null) {
            return false;
        }
        switch (stored.getType()) {
            case AMOUNT:
                return validateAmountStored(stored, constraintValidatorContext);
            case TRACKED:
                return validateTrackedStored(stored, constraintValidatorContext);
            default:
                return false;
        }
    }
}
