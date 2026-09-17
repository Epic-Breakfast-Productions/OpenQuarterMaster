package tech.ebp.oqm.plugin.mssController.testResources.modules.modInterfaces.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleInterface;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Optional;

@Slf4j
public class NetTestModuleInterface  extends TestModuleInterface {

	@Getter(AccessLevel.PROTECTED)
	private HttpServer server;

	public NetTestModuleInterface(ObjectMapper objectMapper, TestModuleEngine engine) throws IOException {
		super(objectMapper, engine);
	}



	void handleCommandRequest(HttpExchange exchange) throws IOException {
		//TODO:: validate authorization

		String command;
		try (InputStream is = exchange.getRequestBody()) {
			command = new String(is.readAllBytes());
		}

		String response = this.getEngine().handleData(command);

		exchange.sendResponseHeaders(200, response.length());
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(response.getBytes());
		}
	}


	@Override
	public void init() throws IOException {
		this.server = HttpServer.create(
			new InetSocketAddress(0),
			0
		);

		server.createContext("/command", this::handleCommandRequest);
		server.start();
	}

	@Override
	public void send(String message) {

	}

	@Override
	public Optional<String> receive() {
		return Optional.empty();
	}

	@Override
	public void close() throws Exception {
		this.server.stop(0);
	}

	public String getAddress() {
		return "http://localhost:" + this.server.getAddress().getPort();
	}
}
