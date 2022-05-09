package com.ebp.openQuarterMaster.plugin;

import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@Traced
@ApplicationScoped
public class ResponseCreator {

	public String getResponse(int i){
		return "Hello "+i+"; " + UUID.randomUUID();
	}
}
