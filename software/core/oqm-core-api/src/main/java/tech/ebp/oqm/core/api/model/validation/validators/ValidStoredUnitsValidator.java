package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidHeldStoredUnits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//TODO:: turn back to AmountItem type when https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578 resolved
public class ValidStoredUnitsValidator
	extends Validator<ValidHeldStoredUnits, InventoryItem>
{
	
	public static final String INVALID_UNITS_FOUND_FORMAT = "Found %d stored objects with units incompatible with the item's.";
	
	@Override
	public boolean isValid(
		InventoryItem item,
		ConstraintValidatorContext constraintValidatorContext
	) {
		List<String> validationErrs = new ArrayList<>();

		//TODO:: rework
//		Stream<AmountStored> storedStream = (Stream<AmountStored>) item.storedStream();
//
//		long invalidCount = storedStream.filter(
//			(AmountStored curStored)->{
//				return !item.getUnit().isCompatible(curStored.getAmount().getUnit());
//			}
//		).count();
//
//		if (invalidCount > 0) {
//			validationErrs.add(String.format(INVALID_UNITS_FOUND_FORMAT, invalidCount));
//		}
		
		return this.processValidationResults(validationErrs, constraintValidatorContext);
	}
}
