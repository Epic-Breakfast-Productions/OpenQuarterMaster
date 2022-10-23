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
		
		Stream<AmountStored> storedStream = (Stream<AmountStored>) item.storedStream();
		
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
