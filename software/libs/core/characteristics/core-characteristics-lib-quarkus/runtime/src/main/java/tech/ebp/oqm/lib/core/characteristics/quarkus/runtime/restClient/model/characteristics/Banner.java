package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.characteristics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Banner {
	private String text;
	private String textColor;
	private String backgroundColor;
}
