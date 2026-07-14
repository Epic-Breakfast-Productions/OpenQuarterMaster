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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class XChartGraphService implements GraphProvider {

    @Override
    public byte[] getGraph(List<Transactions> transactions) throws IOException {
        return this.toByteArray(createChart(transactions));
    }

    private XYChart createChart(List<Transactions> transactionsList) {
        XYChart chart = new XYChartBuilder()
            .width(1280)
            .height(720)
            .theme(Styler.ChartTheme.GGPlot2)
            .title("Item value over time")
            .xAxisTitle("Date")
            .yAxisTitle("Units in stock")
            .build();

        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setDatePattern("dd-MM-yyyy HH:mm");

        List<Date> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        for (Transactions transaction : transactionsList) {
            xData.add(Date.from(transaction.timestamp()));
            yData.add(transaction.value());
        }

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
