package com.ebp.openQuarterMaster.plugin.demo.quarkus;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@Slf4j
@Traced
@ApplicationScoped
public class ResponseCreator {
	
	@Inject
	@RestClient
	DemoServiceCaller demoServiceCaller;

	public String getResponse(int i){
		log.info("Getting regular response: {}", i);
		return "Hello "+i+"; " + UUID.randomUUID();
	}
	
	public String getRecursiveResponse(int current, String id){
		log.info("Getting recursive response: {}", current);
		if(current <= 0){
			log.info("Base case!");
			return "/0 " + UUID.randomUUID();
		}
		
		log.info("Recursive case!");
		
		String data;
		try {
			data = demoServiceCaller.recurse(id, current - 1);
		}catch(Exception e){
			log.warn("Error from rest call in recursive case: ", e);
			data = "Error: " + e.getLocalizedMessage();
		}
		
		return "/" + current + data;
	}
}
