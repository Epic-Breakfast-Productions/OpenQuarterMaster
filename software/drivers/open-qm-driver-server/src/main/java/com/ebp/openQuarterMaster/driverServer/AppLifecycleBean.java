package com.ebp.openQuarterMaster.driverServer;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@Slf4j
@ApplicationScoped
@QuarkusMain
public class AppLifecycleBean {
	void onStart(@Observes StartupEvent ev) {
		log.info("The application is starting...");
	}
	
	void onStop(@Observes ShutdownEvent ev) {
		log.info("The application is stopping...");
	}
	
	public static void main(String ... args) {
		System.out.println("Running main method");
		Quarkus.run(args);
	}
}
