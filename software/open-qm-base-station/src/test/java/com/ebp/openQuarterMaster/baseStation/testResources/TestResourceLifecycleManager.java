package com.ebp.openQuarterMaster.baseStation.testResources;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.runtime.Mongod;
import de.flapdoodle.embed.process.runtime.Network;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO:: make better with configuration
 */
@Slf4j
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
	private static final boolean SELENIUM_HEADLESS = true;
	
	private static volatile MongodExecutable MONGO_EXE = null;

	private static volatile WebDriver webDriver;
	
	static {
		WebDriverManager.firefoxdriver().setup();
	}
	
	public static synchronized void startMongoTestServer() throws IOException {
		if(MONGO_EXE != null) {
			log.info("Flapdoodle Mongo already started.");
			return;
		}
		Version.Main version = Version.Main.V4_0;
		int port = 27018;
		log.info("Starting Flapdoodle Test Mongo {} on port {}", version, port);
		IMongodConfig config = new MongodConfigBuilder()
			.version(version)
			.net(new Net(port, Network.localhostIsIPv6()))
			.build();
		try{
			MONGO_EXE = MongodStarter.getDefaultInstance().prepare(config);
			MongodProcess process = MONGO_EXE.start();
			if(!process.isProcessRunning()){
				throw new IOException();
			}
		} catch (Throwable e){
			log.error("FAILED to start test mongo server: ", e);
			MONGO_EXE = null;
			throw e;
		}
	}
	
	public static synchronized void stopMongoTestServer() {
		if(MONGO_EXE == null) {
			log.warn("Mongo was not started.");
			return;
		}
		MONGO_EXE.stop();
		MONGO_EXE = null;
	}
	
	public synchronized static void cleanMongo() throws IOException {
		if(MONGO_EXE == null) {
			log.warn("Mongo was not started.");
			return;
		}
		
		log.info("Cleaning Mongo of all entries.");
	}
	
	
	public static synchronized boolean webDriverIsInitted() {
		return getWebDriver() != null;
	}
	
	public static synchronized void initWebDriver() {
		if(!webDriverIsInitted()) {
			log.info("Opening web browser");
			webDriver = new FirefoxDriver(new FirefoxOptions().setHeadless(SELENIUM_HEADLESS));
		} else {
			log.info("Driver already started.");
		}
	}
	
	public static synchronized WebDriver getWebDriver() {
		return webDriver;
	}
	
	@Override
	public Map<String, String> start() {
		try {
			startMongoTestServer();
		} catch(IOException e) {
			log.error("Unable to start Flapdoodle Mongo server");
		}
		
		initWebDriver();
		
		return new HashMap<>();
	}
	
	@Override
	public void stop() {
		stopMongoTestServer();
		log.info("Closing web driver.");
		getWebDriver().close();
	}
}
