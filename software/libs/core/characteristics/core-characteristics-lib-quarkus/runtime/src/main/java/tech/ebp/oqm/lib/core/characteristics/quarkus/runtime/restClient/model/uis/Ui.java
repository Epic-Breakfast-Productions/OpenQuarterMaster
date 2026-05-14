package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.uis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ui {
	private Integer order;
	private String id;
	private String name;
	private String description;
	private String baseUri;
	private boolean icon;
}
