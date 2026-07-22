package tech.ebp.oqm.core.baseStation.service.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.util.Iterator;

public abstract class GraphProvider {

	protected XYChartBuilder getChartBuilder(
		String title
	){
		return new XYChartBuilder()
							.width(1280)
							.height(720)
							.theme(Styler.ChartTheme.GGPlot2)
							.title(title)
			;
	}

}
