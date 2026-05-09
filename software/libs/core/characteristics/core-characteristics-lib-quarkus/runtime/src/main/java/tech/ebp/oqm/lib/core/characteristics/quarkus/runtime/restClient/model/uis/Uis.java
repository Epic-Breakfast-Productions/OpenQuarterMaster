package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.uis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Uis {
	private String home;
	private List<Ui> core;
	private List<Ui> plugin;
	private List<Ui> metrics;
	private List<Ui> infra;
	
	public List<Ui> getUiCategory(String category){
		return switch (category) {
			case "core" -> this.core;
			case "plugin" -> this.plugin;
			case "metrics" -> this.metrics;
			case "infra" -> this.infra;
			default -> throw new IllegalArgumentException("Invalid category: " + category);
		};
	}
}
