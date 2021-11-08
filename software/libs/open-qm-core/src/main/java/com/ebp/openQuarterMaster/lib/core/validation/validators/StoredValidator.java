package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidStored;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;


public class StoredValidator extends Validator implements ConstraintValidator<ValidStored, Stored> {

    public static final String ITEMS_LIST_NOT_NULL = "Items list in amount stored was not null";
    public static final String AMOUNT_WAS_NULL = "Amount in amount stored was null";
    public static final String ITEM_LIST_WAS_NULL = "Item list in tracked stored was null.";
    public static final String TYPE_WAS_NULL = "Type was null";

    private void validateAmountStored(Stored stored, List<String> errs) {
        if (stored.getItems() != null) {
            errs.add(ITEMS_LIST_NOT_NULL);
        }
        if (stored.getAmount() == null) {
            errs.add(AMOUNT_WAS_NULL);
        }
    }

    private void validateTrackedStored(Stored stored, List<String> errs) {
        if (stored.getItems() == null) {
            errs.add(ITEM_LIST_WAS_NULL);
        }
    }

    @Override
    public boolean isValid(Stored stored, ConstraintValidatorContext constraintValidatorContext) {
        List<String> validationErrs = new ArrayList<>();
        if (stored != null) {
            if (stored.getType() == null) {
                validationErrs.add(TYPE_WAS_NULL);
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
