package tech.ebp.oqm.core.baseStation.service.graph;

import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

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
