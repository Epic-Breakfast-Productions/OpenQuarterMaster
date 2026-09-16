package tech.ebp.oqm.core.api.model.units;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.measure.Quantity;
import javax.measure.Unit;

/**
 * TODO:: custom validator to ensure compatibility
 */
@Data
@Builder
public class ConvertRequest {
	@NonNull
	@NotNull
	private Quantity quantity;
	@NonNull
	@NotNull
	private Unit newUnit;
}
