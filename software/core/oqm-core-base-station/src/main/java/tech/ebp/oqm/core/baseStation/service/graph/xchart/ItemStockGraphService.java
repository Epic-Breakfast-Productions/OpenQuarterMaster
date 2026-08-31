package tech.ebp.oqm.core.baseStation.service.graph.xchart;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.knowm.xchart.ChartEncoder;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import tech.ebp.oqm.core.baseStation.model.graph.ItemNameTransactionIterator;
import tech.ebp.oqm.core.baseStation.model.graph.TransactionGraphValue;
import tech.ebp.oqm.core.baseStation.service.graph.GraphProvider;
import tech.ebp.oqm.core.baseStation.service.graph.TransactionMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class ItemStockGraphService extends GraphProvider {

    public byte[] getGraph(List<ItemNameTransactionIterator> itemNameTransactionIterators) throws IOException {
        return this.toByteArray(createChart(itemNameTransactionIterators));
    }

    private XYChart createChart(List<ItemNameTransactionIterator> transactionsIterator) {
        XYChart chart = this.getChartBuilder("Item Stock over time")
            .xAxisTitle("Date")
            .yAxisTitle("Amount in stock")
            .build();

        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setDatePattern("dd-MM-yyyy HH:mm");

        List<Date> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        for (ItemNameTransactionIterator itemNameTransactionIterator : transactionsIterator) {
            while (itemNameTransactionIterator.iterator().hasNext()) {
                ObjectNode page = itemNameTransactionIterator.iterator().next();
                for (TransactionGraphValue transaction : TransactionMapper.mapTransactionsToArray(page)) {
                    xData.add(Date.from(transaction.timestamp()));
                    yData.add(transaction.value());
                }
            }

            if(!xData.isEmpty() && !yData.isEmpty()) {
                XYSeries series = chart.addSeries("Item: " + itemNameTransactionIterator.name(), xData, yData);
                //TODO: add logic to hash name to get color of the line and cache it if needer (awt colors)
                series.setMarker(SeriesMarkers.CIRCLE);
            }

            xData.clear();
            yData.clear();
        }

        return chart;
    }

    private byte[] toByteArray(XYChart chart) throws IOException {
        ByteArrayOutputStream heapSvg = new ByteArrayOutputStream();
        ChartEncoder.saveChart(chart, heapSvg, "svg");
        return heapSvg.toByteArray();
    }
}
