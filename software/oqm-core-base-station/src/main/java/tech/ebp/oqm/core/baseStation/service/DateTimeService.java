package tech.ebp.oqm.core.baseStation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

@Named("DateTimeService")
@ApplicationScoped
public class DateTimeService {
	
	private final DateTimeFormatter formatter;
	
	@Inject
	public DateTimeService(
		@ConfigProperty(name = "service.dateTimeFormat.default")
		String datetimeFormat
	){
		this.formatter = DateTimeFormatter.ofPattern(datetimeFormat);
	}
	
	public String formatForUi(Date date){
		if (date == null) {
			date = new Date();
		}
		return formatter.format(date.toInstant());
	}
	
	public String formatForUi(TemporalAccessor date){
		if(date == null){
			date = ZonedDateTime.now();
		}
		return formatter.format(date);
	}
	
	public String formatForUi(String dateTime) {
		return this.formatForUi(ZonedDateTime.parse(dateTime));
	}
	
	public String formatForUi(){
		return this.formatForUi((TemporalAccessor) null);
	}
}
