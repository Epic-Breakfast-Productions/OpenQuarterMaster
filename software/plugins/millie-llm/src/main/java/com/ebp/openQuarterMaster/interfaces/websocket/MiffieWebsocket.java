package com.ebp.openQuarterMaster.interfaces.websocket;

import com.ebp.openQuarterMaster.interfaces.InterfaceConstants;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebSocket(path = InterfaceConstants.WEBSOCKET_ROOT + "/miffie")
public class MiffieWebsocket {

	@Inject
	WebSocketConnection connection;

	@OnOpen
	public String onOpen() {
		log.info("Opened new WebSocket connection; {}", connection.id());
		return "Welcome to OQM! How can I help you today?";
	}

	@OnTextMessage
	public String onTextMessage(String message) {
		log.info("Got new websocket message; {}", connection.id());
		return "I'm Miffie! I Heard: " + message;
	}
}
