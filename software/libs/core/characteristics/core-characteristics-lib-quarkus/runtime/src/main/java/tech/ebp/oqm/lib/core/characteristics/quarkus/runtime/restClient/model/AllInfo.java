package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.characteristics.Characteristics;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.uis.Ui;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.uis.Uis;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllInfo {
	private Characteristics characteristics;
	private Uis ui;
}
