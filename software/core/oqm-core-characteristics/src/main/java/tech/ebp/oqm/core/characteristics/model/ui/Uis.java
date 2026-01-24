package tech.ebp.oqm.core.characteristics.model.ui;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Uis {
	
	@NonNull
	@Builder.Default
	private List<Ui> core = new ArrayList<>();
	
	@NonNull
	@Builder.Default
	private List<Ui> infra = new ArrayList<>();
	
	@NonNull
	@Builder.Default
	private Map<String, Ui> plugins = new LinkedHashMap<>();
	
	//TODO:: logo?
}
