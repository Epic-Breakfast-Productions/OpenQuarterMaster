package tech.ebp.oqm.core.baseStation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Named("QDS")
public class QuteDebugService {
	
	public String debug(String message, Object... objects) {
		log.debug(message, objects);
		return "";
	}
}
