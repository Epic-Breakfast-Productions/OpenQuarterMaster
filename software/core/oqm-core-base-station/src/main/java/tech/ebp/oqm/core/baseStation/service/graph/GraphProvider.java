package tech.ebp.oqm.core.baseStation.service.graph;

import tech.ebp.oqm.core.baseStation.model.graph.Transactions;

import java.io.IOException;
import java.util.List;

public interface GraphProvider {
    byte[] getGraph(List<Transactions> transactions) throws IOException;
}
