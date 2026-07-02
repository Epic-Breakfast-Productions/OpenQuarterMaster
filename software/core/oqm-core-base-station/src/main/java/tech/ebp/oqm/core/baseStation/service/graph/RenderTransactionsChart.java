package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import tech.ebp.oqm.core.baseStation.model.Transactions;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.util.stream.Collectors;

@ApplicationScoped
public class enderTransactionsChart {
    public byte[] transactions(ObjectNode jsonResponse) {
        List<Transactions> transactionsList = TransactionsMapper.mapTransactionsToArray(jsonResponse);
        try {
            return TransactionsMapper.toByteArray(this.createChart(transactionsList));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render transactions chart as SVG.", e);
        }
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
            .collect(Collectors.toList());

        List<Integer> yData = transactionsList.stream()
            .map(Transactions::value)
            .collect(Collectors.toList());

        XYSeries series = chart.addSeries("Transaction", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        return chart;
    }
}
