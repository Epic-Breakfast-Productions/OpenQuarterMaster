package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.object.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.object.storage.items.ListAmountItem;
import com.ebp.openQuarterMaster.lib.core.object.storage.items.SimpleAmountItem;
import com.ebp.openQuarterMaster.lib.core.object.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidHeldStoredUnits;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//TODO:: turn back to AmountItem type when https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578 resolved
public class ValidStoredUnitsValidator extends Validator<ValidHeldStoredUnits, InventoryItem> {

	public static final String INVALID_UNITS_FOUND_FORMAT = "Found %d stored objects with units incompatible with the item's.";

	@Override
	public boolean isValid(InventoryItem item, ConstraintValidatorContext constraintValidatorContext) {
		List<String> validationErrs = new ArrayList<>();


		Stream<AmountStored> storedStream;
		if (item instanceof SimpleAmountItem) {
			storedStream = ((SimpleAmountItem) item)
				.getStorageMap()
				.values()
				.stream();
		} else if (item instanceof ListAmountItem) {
			storedStream = ((ListAmountItem) item)
				.getStorageMap()
				.values()
				.stream()
				.flatMap(List::stream);
		} else {
			validationErrs.add("Unsupported type of AmountStored object giver: " + item.getClass().getName());
			storedStream = Stream.empty();
		}

		long invalidCount = storedStream.filter(
			(AmountStored curStored)->{
				return !item.getUnit().isCompatible(curStored.getAmount().getUnit());
			}
		).count();

		if (invalidCount > 0) {
			validationErrs.add(String.format(INVALID_UNITS_FOUND_FORMAT, invalidCount));
		}

		return this.processValidationResults(validationErrs, constraintValidatorContext);
	}
}
