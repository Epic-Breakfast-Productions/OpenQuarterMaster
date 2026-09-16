package tech.ebp.oqm.core.baseStation.service.printout;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import java.time.ZonedDateTime;

public abstract class PrintoutDataService {

	protected TemplateInstance setupBasicPrintoutData(
		Template template
	){
		return template.data("generateDatetime", ZonedDateTime.now());
	}

}