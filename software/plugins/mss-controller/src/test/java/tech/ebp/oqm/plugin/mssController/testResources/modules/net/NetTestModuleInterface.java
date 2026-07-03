package tech.ebp.oqm.plugin.mssController.testResources.modules.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModule;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Optional;

@Slf4j
public class NetTestModuleInterface  extends TestModuleInterface {

	@Getter(AccessLevel.PROTECTED)
	private HttpServer server;

	public NetTestModuleInterface(ObjectMapper objectMapper) throws IOException {
		super(objectMapper);
	}

	void handleRequest(HttpExchange exchange) throws IOException {

		exchange.getRequestBody();




		// Define the plain-text message response
		String response = "Hello from Java 25 HttpServer!";
		byte[] responseBytes = response.getBytes();

		// Set HTTP status code 200 (OK) and content length
		exchange.sendResponseHeaders(200, responseBytes.length);

		// Write the message payload to the output stream
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(responseBytes);
		}
	}


	@Override
	public void init(TestModule module) throws IOException {
		this.server = HttpServer.create(
			new InetSocketAddress(0),
			0
		);

		server.createContext("/", this::handleRequest);
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
