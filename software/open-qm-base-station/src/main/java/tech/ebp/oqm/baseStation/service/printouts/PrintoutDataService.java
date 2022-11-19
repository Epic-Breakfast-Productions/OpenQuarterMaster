package tech.ebp.oqm.baseStation.service.printouts;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import tech.ebp.oqm.baseStation.interfaces.ui.UiUtils;

import java.time.ZonedDateTime;

public abstract class PrintoutDataService {
	
	
	
	
	protected TemplateInstance setupBasicPrintoutData(
		Template template
	){
		return template.data("generateDatetime", ZonedDateTime.now())
					.data("dateTimeFormatter", UiUtils.DATE_TIME_FORMATTER);
	}
	
}
