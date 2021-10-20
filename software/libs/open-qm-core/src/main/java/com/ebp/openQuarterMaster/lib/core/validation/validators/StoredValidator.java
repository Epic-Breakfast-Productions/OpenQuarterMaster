package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.storage.stored.Stored;
import com.ebp.openQuarterMaster.lib.core.storage.stored.TrackedItem;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.stream.Collectors;


public class StoredValidator implements ConstraintValidator<ValidUnit, Stored> {

    private boolean validateAmountStored(Stored stored){
        return stored.getAmount() != null &&
                stored.getItems() == null;
    }
    private boolean validateTrackedStored(Stored stored){
        if(!(
                stored.getItems() != null &&
                stored.getAmount() == null
        )){
          return false;
        }
        //tracked items cannot have null or empty keys or values
        if(stored.getItems()
                .keySet()
                .stream()
                .anyMatch((String key)->{
                    return key == null && key.isBlank();
                })
        ){
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
        if(stored == null){
            return false;
        }
        if(stored.getType() == null){
            return false;
        }
        switch (stored.getType()){
            case AMOUNT:
                return validateAmountStored(stored);
            case TRACKED:
                return validateTrackedStored(stored);
            default:
                return false;
        }
    }
}
