package com.ebp.openQuarterMaster.lib.core.rest.externalItemLookup;

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
public class ExtItemLookupResults {
	private List<ExtItemLookupResult> results;
	private Map<String, Throwable> serviceErrs;
}
