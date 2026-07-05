package tech.ebp.oqm.core.baseStation.service.graph.xchart;

import jakarta.enterprise.context.ApplicationScoped;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import tech.ebp.oqm.core.baseStation.model.graph.Transactions;
import tech.ebp.oqm.core.baseStation.service.graph.GraphProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class XChartGraphService implements GraphProvider {

    @Override
    public byte[] getGraph(List<Transactions> transactions) throws IOException {
        return this.toByteArray(createChart(transactions));
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
        chart.getStyler().setDatePattern("dd-MM-yyyy HH:mm");

        List<Instant> xData = transactionsList.stream()
            .map(Transactions::timestamp)
            .toList();

        List<Integer> yData = transactionsList.stream()
            .map(Transactions::value)
            .toList();

        XYSeries series = chart.addSeries("Transaction", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        return chart;
    }

    private byte[] toByteArray(XYChart chart) throws IOException {
        ByteArrayOutputStream heapSvg = new ByteArrayOutputStream();
        VectorGraphicsEncoder.saveVectorGraphic(chart, heapSvg, VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
        return heapSvg.toByteArray();
    }
}
