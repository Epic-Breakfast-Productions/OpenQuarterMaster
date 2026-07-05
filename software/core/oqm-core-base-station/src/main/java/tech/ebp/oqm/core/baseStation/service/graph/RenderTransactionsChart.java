package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;
import tech.ebp.oqm.core.baseStation.model.Transactions;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.AppliedTransactionSearch;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.io.IOException;

@ApplicationScoped
public class RenderTransactionsChart extends ApiProvider implements GraphProvider {
    @Override
    public byte[] getGraph(String dbIdOrName, String itemId, String startDate, String endDate) {
        ObjectNode jsonResponse = this.getTransactions(dbIdOrName, itemId);
        List<Transactions> transactionsList = TransactionsMapper.mapTransactionsToArray(jsonResponse);
        try {
            return TransactionsMapper.toByteArray(this.createChart(transactionsList));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render transactions chart as SVG.", e);
        }
    }
    
    private ObjectNode getTransactions(String dbIdOrName, String itemId) {
        return this.getOqmCoreApiClient()
            .invItemStoredTransactionSearch(this.getBearerHeaderStr(), dbIdOrName, itemId, new AppliedTransactionSearch())
            .await()
            .indefinitely();
    }

    private XYChart createChart(List<Transactions> transactionsList) {
        XYChart chart = new XYChartBuilder()
            .width(1000)
            .height(800)
            .theme(Styler.ChartTheme.GGPlot2)
            .title("Transactions Over Time")
            .xAxisTitle("Timestamp")
            .yAxisTitle("Value")
            .build();

        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setDatePattern("yyyy-MM-dd HH:mm");

        List<Date> xData = transactionsList.stream()
            .map(t -> Date.from(ZonedDateTime.parse(t.timestamp()).toInstant()))
            .toList();

        List<Integer> yData = transactionsList.stream()
            .map(Transactions::value)
            .toList();

        XYSeries series = chart.addSeries("Transaction", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        return chart;
    }
}
    //FIXME: add some abstaction to not depend on one specific API of charting lib
