package com.ebp.openQuarterMaster.lib.core.rest.productLookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProductLookupResults {
	private List<ProductLookupResult> results;
	private Map<String, Throwable> serviceErrs;
}
