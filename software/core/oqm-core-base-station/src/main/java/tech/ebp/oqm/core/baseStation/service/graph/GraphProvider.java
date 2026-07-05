package tech.ebp.oqm.core.baseStation.service.graph;

public interface GraphProvider {
    byte[] getGraph(String database, String itemId, String startDate, String endDate);
}
