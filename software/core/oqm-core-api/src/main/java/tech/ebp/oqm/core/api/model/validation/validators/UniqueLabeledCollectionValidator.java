package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.object.Labeled;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.model.validation.annotations.UniqueLabeledCollection;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUnit;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UniqueLabeledCollectionValidator extends Validator<UniqueLabeledCollection, Collection<? extends Labeled>> {
	
	@Override
	public boolean isValid(Collection<? extends Labeled> collection, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (collection == null) {
			return true;
		} else {
			Set<String> elements = new HashSet<>();
			
			List<String> duplicateLabels = collection.stream()
											   .map(Labeled::getLabel)
											   .filter(n->!elements.add(n))
											   .toList();
			
			if (!duplicateLabels.isEmpty()) {
				errs.add("Entry labels with more than one instance found: " + String.join(", ", duplicateLabels));
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
