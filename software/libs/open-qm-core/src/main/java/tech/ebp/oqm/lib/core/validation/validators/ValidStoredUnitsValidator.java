package tech.ebp.oqm.lib.core.validation.validators;

import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.ListAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.SimpleAmountItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.ListStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.SingleStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;
import tech.ebp.oqm.lib.core.validation.annotations.ValidHeldStoredUnits;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

//TODO:: turn back to AmountItem type when https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578 resolved
public class ValidStoredUnitsValidator
	extends Validator<ValidHeldStoredUnits, InventoryItem<? extends AmountStored, ?, ? extends StoredWrapper<?, AmountStored>>>
{
	
	public static final String INVALID_UNITS_FOUND_FORMAT = "Found %d stored objects with units incompatible with the item's.";
	
	@Override
	public boolean isValid(
		InventoryItem<? extends AmountStored, ?, ? extends StoredWrapper<?, AmountStored>> item,
		ConstraintValidatorContext constraintValidatorContext
	) {
		List<String> validationErrs = new ArrayList<>();
		
		List<StoredWrapper<?, AmountStored>> storedWrapperList = new ArrayList<>(item.getStorageMap().values());
		
		Stream<AmountStored> storedStream;
		if (item instanceof SimpleAmountItem) {
			storedStream =
				((List<StoredWrapper<?, AmountStored>>) storedWrapperList)
					.stream()
					.map(
						(StoredWrapper<?, AmountStored> w)->(AmountStored) w.getStored()
					);
		} else if (item instanceof ListAmountItem) {
			storedStream =
				(storedWrapperList)
					.stream()
					.parallel()
					.map(
						(StoredWrapper<?, AmountStored> w)->(List<AmountStored>) w.getStored()
					)
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
