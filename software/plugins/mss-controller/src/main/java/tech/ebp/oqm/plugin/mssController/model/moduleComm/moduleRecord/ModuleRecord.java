package tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModuleRecord {
	@Id
	@NotNull
	@NonNull
	private String serialId;

	@Builder.Default
	private List<String> inDbs = new ArrayList<>();
}
