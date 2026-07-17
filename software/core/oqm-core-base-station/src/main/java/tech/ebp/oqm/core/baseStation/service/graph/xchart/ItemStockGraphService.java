package tech.ebp.oqm.core.baseStation.service.graph.xchart;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import tech.ebp.oqm.core.baseStation.model.graph.Transactions;
import tech.ebp.oqm.core.baseStation.service.graph.GraphProvider;
import tech.ebp.oqm.core.baseStation.service.graph.TransactionMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@ApplicationScoped
public class ItemStockGraphService extends GraphProvider {

    public byte[] getGraph(ObjectNode item, Iterator<ObjectNode> transactionsIterator) throws IOException {
        return this.toByteArray(createChart(transactionsIterator));
    }

    private XYChart createChart(Iterator<ObjectNode> transactionsIterator) {
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
        while (transactionsIterator.hasNext()) {
            ObjectNode page = transactionsIterator.next();
            for (Transactions transaction : TransactionMapper.mapTransactionsToArray(page)) {
                xData.add(Date.from(transaction.timestamp()));
                yData.add(transaction.value());
            }
        }

        XYSeries series = chart.addSeries("Item Stock", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        return chart;
    }

    private byte[] toByteArray(XYChart chart) throws IOException {
        ByteArrayOutputStream heapSvg = new ByteArrayOutputStream();
        VectorGraphicsEncoder.saveVectorGraphic(chart, heapSvg, VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
        return heapSvg.toByteArray();
    }
}
