package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.Labeled;
import tech.ebp.oqm.core.api.model.object.storage.items.StorageBlockSettings;
import tech.ebp.oqm.core.api.model.validation.annotations.UniqueLabeledCollection;
import tech.ebp.oqm.core.api.model.validation.annotations.UniqueStorageBlockSettingsCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueStorageBlockSettingsCollectionValidator extends Validator<UniqueStorageBlockSettingsCollection, Collection<StorageBlockSettings>> {

	@Override
	public boolean isValid(Collection<StorageBlockSettings> collection, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();

		if (collection == null) {
			return true;
		} else {
			if(collection.stream().anyMatch(sbs -> sbs.getStorageBlock() == null)){
				errs.add("Storage block settings cannot have a null storage block");
			} else {

				Set<ObjectId> elements = new HashSet<>();

				List<String> duplicateStorageBlocks = collection.stream()
														  .map(StorageBlockSettings::getStorageBlock)
														  .filter(n->!elements.add(n))
														  .map(ObjectId::toString)
														  .toList();

				if (!duplicateStorageBlocks.isEmpty()) {
					errs.add("Multiple storage block settings found with the same storage block: " + String.join(", ", duplicateStorageBlocks));
				}
			}
		}

		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
