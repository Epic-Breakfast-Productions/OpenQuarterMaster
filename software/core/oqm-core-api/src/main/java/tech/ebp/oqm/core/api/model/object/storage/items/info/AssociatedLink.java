package tech.ebp.oqm.core.api.model.object.storage.items.info;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.Labeled;

import java.net.URI;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AssociatedLink implements Labeled {
	
	@NonNull
	@NotNull
	private String label;
	
	@NonNull
	@NotNull
	private URI link;
	
	@Builder.Default
	private String description = null;
}
