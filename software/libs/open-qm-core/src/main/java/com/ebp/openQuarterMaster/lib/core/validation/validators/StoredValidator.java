package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidStored;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class StoredValidator extends Validator implements ConstraintValidator<ValidStored, Stored> {

    private void validateAmountStored(Stored stored, List<String> errs) {
        if (stored.getItems() != null) {
            errs.add("Items list in amount stored was not null");
        }
        if(stored.getAmount() == null){
            errs.add("Amount in amount stored was null");
        }
    }

    private void validateTrackedStored(Stored stored, List<String> errs) {
        if (stored.getItems() == null) {
            errs.add("Item list in tracked stored was null.");
        }
    }

    @Override
    public boolean isValid(Stored stored, ConstraintValidatorContext constraintValidatorContext) {
        List<String> validationErrs = new ArrayList<>();
        if (stored == null) {
            validationErrs.add("Stored object was null");
        } else {
            if (stored.getType() == null) {
                validationErrs.add("Type was null");
            } else {
                switch (stored.getType()) {
                    case AMOUNT:
                        validateAmountStored(stored, validationErrs);
                        break;
                    case TRACKED:
                        validateTrackedStored(stored, validationErrs);
                        break;
                    default:
                        validationErrs.add("Unsupported stored type: " + stored.getType().name());
                }
            }
        }
        return this.processValidationResults(validationErrs, constraintValidatorContext);
    }
}
