package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidInventoryItem;
import tech.units.indriya.AbstractUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator that validates the state of an {@link InventoryItem}.
 *
 * TODO:: test
 */
public class InventoryItemValidator extends Validator implements ConstraintValidator<ValidInventoryItem, InventoryItem> {

    private void validateAmountItem(InventoryItem item, List<String> errs){
        boolean typeMismatch = false;
        boolean incompatibleAmount = false;
        for(Stored stored : item.getStorageMap().values()) {
            //all stored items need to match amount
            if(!typeMismatch && !StoredType.AMOUNT.equals(stored.getType())){
                typeMismatch = true;
            }
            //all stored items need to be a compatible unit
            if(!incompatibleAmount && !item.getUnit().isCompatible(stored.getAmount().getUnit())){
                incompatibleAmount = true;
            }
            if(typeMismatch && incompatibleAmount){
                break;
            }
        }
        if(typeMismatch){
            errs.add("Item has storage(s) that are not AMOUNT");
        }
        if(incompatibleAmount){
            errs.add("Item has storage(s) that have incompatible units with the item");
        }
    }

    private void validateTrackedItem(InventoryItem item, List<String> errs){
        //unit of item must be ONE
        if(!AbstractUnit.ONE.equals(item.getUnit())){
            errs.add("The unit for the tracked item was not ONE");
        }
        //all stored items need to match tracked
        if(!item.getStorageMap().values().stream().allMatch((Stored stored) -> {
                return StoredType.TRACKED.equals(stored.getType());
            })){
            errs.add("Not all stored values were of TRACKED type.");
        }
    }

    @Override
    public boolean isValid(InventoryItem item, ConstraintValidatorContext constraintValidatorContext) {
        List<String> validationErrs = new ArrayList<>();
        if(item == null){
            validationErrs.add("Item was null");
        }else {
            if (item.getStoredType() == null) {
                validationErrs.add("Stored type was null.");
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
