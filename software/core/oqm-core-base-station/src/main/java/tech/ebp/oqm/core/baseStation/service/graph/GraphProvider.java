package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;

public interface GraphProvider {
    byte[] getGraph(Iterator<ObjectNode> transactionsIterator) throws IOException;
}
