package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidInventoryItem;
import tech.units.indriya.AbstractUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator that validates the state of an {@link InventoryItem}.
 */
public class InventoryItemValidator extends Validator implements ConstraintValidator<ValidInventoryItem, InventoryItem> {

    public static final String STORED_TYPE_WAS_NULL = "Stored type was null";
    public static final String NOT_AMOUNT = "Item has storage(s) that are not AMOUNT";
    public static final String INCOMPATIBLE_UNITS = "Item has storage(s) that have incompatible units with the item";
    public static final String TRACKED_IDENTIFIER_NAME_WHEN_IT_SHOULDN_T = "Item has a tracked identifier name when it shouldn't";
    public static final String NOT_ONE = "The unit for the tracked item was not ONE";
    public static final String NOT_ALL_TRACKED_TYPE = "Not all stored values were of TRACKED type.";

    private void validateAmountItem(InventoryItem item, List<String> errs) {
        boolean typeMismatch = false;
        boolean incompatibleAmount = false;
        for (List<Stored> storedList : item.getStorageMap().values()) {
            for (Stored stored : storedList) {
                //all stored items need to match amount
                if (!typeMismatch && !StoredType.AMOUNT.equals(stored.getType())) {
                    typeMismatch = true;
                }
                //all stored items need to be a compatible unit
                if (!incompatibleAmount && !item.getUnit().isCompatible(stored.getAmount().getUnit())) {
                    incompatibleAmount = true;
                }
                if (typeMismatch && incompatibleAmount) {
                    break;
                }
            }
            if (typeMismatch && incompatibleAmount) {
                break;
            }
        }
        if (typeMismatch) {
            errs.add(NOT_AMOUNT);
        }
        if (incompatibleAmount) {
            errs.add(INCOMPATIBLE_UNITS);
        }
        if (item.getTrackedItemIdentifierName() != null) {
            errs.add(TRACKED_IDENTIFIER_NAME_WHEN_IT_SHOULDN_T);
        }
    }

    private void validateTrackedItem(InventoryItem item, List<String> errs) {
        //unit of item must be ONE
        if (!AbstractUnit.ONE.equals(item.getUnit())) {
            errs.add(NOT_ONE);
        }
        //all stored items need to match tracked
        for (List<Stored> storedList : item.getStorageMap().values()) {
            for (Stored curStored : storedList) {
                if (!StoredType.TRACKED.equals(curStored.getType())) {
                    errs.add(NOT_ALL_TRACKED_TYPE);
                    return;
                }
            }
        }
    }

    @Override
    public boolean isValid(InventoryItem item, ConstraintValidatorContext constraintValidatorContext) {
        List<String> validationErrs = new ArrayList<>();
        if (item != null) {
            if (item.getStoredType() == null) {
                validationErrs.add(STORED_TYPE_WAS_NULL);
            } else {
                switch (item.getStoredType()) {
                    case AMOUNT:
                        validateAmountItem(item, validationErrs);
                        break;
                    case TRACKED:
                        validateTrackedItem(item, validationErrs);
                        break;
                    default:
                        validationErrs.add("Unsupported stored type: " + item.getStoredType().name());
                }
            }
        }
        return this.processValidationResults(validationErrs, constraintValidatorContext);
    }
}
