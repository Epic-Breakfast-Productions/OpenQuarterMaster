package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidInventoryItem;
import tech.units.indriya.AbstractUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InventoryItemValidator implements ConstraintValidator<ValidInventoryItem, InventoryItem> {

    private boolean validateAmountItem(InventoryItem item){
        for(Stored stored : item.getStorageMap().values()) {
            //all stored items need to match amount
            if(!StoredType.AMOUNT.equals(stored.getType())){
                return false;
            }
            //all stored items need to be a compatible unit
            if(!item.getUnit().isCompatible(stored.getAmount().getUnit())){
                return false;
            }
        }
        return true;
    }

    private boolean validateTrackedItem(InventoryItem item){
        //unit of item must be ONE
        if(!AbstractUnit.ONE.equals(item.getUnit())){
            return false;
        }

        //all stored items need to match tracked
        return item.getStorageMap().values().stream().anyMatch((Stored stored)->{
            return StoredType.TRACKED.equals(stored.getType());
        });
    }

    @Override
    public boolean isValid(InventoryItem item, ConstraintValidatorContext constraintValidatorContext) {
        if(item == null){
            return false;
        }
        if(item.getStoredType() == null){
            return false;
        }
        switch (item.getStoredType()){
            case AMOUNT:
                return validateAmountItem(item);
            case TRACKED:
                return validateTrackedItem(item);
            default:
                return false;
        }
    }
}
