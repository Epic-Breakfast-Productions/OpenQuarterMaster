package com.ebp.openQuarterMaster.plugin;

import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@Traced
@ApplicationScoped
public class ResponseCreator {
	
	@Inject
	@RestClient
	DemoServiceCaller demoServiceCaller;

	public String getResponse(int i){
		return "Hello "+i+"; " + UUID.randomUUID();
	}
	
	public String getRecursiveResponse(int current){
		if(current <= 0){
			return "/0 " + UUID.randomUUID();
		}
		
		String data;
		try {
			data = demoServiceCaller.recurse(current - 1);
		}catch(Exception e){
			data = "Error: " + e.getLocalizedMessage();
		}
		
		return "/" + current + data;
	}
}
