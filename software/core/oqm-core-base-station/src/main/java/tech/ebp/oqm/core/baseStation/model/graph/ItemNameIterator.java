package tech.ebp.oqm.core.baseStation.model.graph;

import tech.ebp.oqm.core.baseStation.service.printout.PrintoutDataSearchUtilService;

public record ItemNameIterator(String name, PrintoutDataSearchUtilService.ResultsIterator iterator) {}
