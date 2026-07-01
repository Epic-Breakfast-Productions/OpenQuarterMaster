package tech.ebp.oqm.plugin.imageSearch.model.search;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.TreeMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
	private String item;

	private TreeMap<Double, String> images;
}
