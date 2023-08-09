package tech.ebp.oqm.baseStation.model.rest.unit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import tech.ebp.oqm.baseStation.model.rest.unit.UnitListEntry;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitListResponse {
	
	@NonNull
	@NotNull
	List<@NotNull UnitListEntry> length = new ArrayList<>();
	@NonNull
	@NotNull
	List<@NotNull UnitListEntry> mass = new ArrayList<>();
	@NonNull
	@NotNull
	List<@NotNull UnitListEntry> area = new ArrayList<>();
	@NonNull
	@NotNull
	List<@NotNull UnitListEntry> volume = new ArrayList<>();
	@NonNull
	@NotNull
	List<@NotNull UnitListEntry> energy = new ArrayList<>();
	@NonNull
	@NotNull
	List<@NotNull UnitListEntry> other = new ArrayList<>();
}
