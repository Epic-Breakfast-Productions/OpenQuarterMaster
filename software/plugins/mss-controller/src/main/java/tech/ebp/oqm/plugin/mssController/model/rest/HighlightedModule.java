package tech.ebp.oqm.plugin.mssController.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HighlightedModule {

	private String serialId;

	private List<Integer> blocks;
}
