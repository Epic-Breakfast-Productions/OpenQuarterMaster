package tech.ebp.oqm.core.baseStation.service.extItemLookup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Named("DateTimeService")
@ApplicationScoped
public class DateTimeService {
	
	public String formatForUi(Date date){
		return "DATETIME";
	}
	
	public String formatForUi(LocalDateTime date){
		return "LDATETIME";
	}
	public String formatForUi(ZonedDateTime date){
		return "ZDATETIME";
	}
}
