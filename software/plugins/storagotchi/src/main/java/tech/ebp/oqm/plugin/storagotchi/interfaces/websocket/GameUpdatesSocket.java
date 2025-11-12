package tech.ebp.oqm.plugin.storagotchi.interfaces.websocket;

import io.quarkus.websockets.next.OnPingMessage;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.Open;
import io.quarkus.websockets.next.UserData;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebSocket(path = "/ws/updates")
public class GameUpdatesSocket {
	
	@Inject
	WebSocketConnection connection;
	
	void connectionOpened(@ObservesAsync @Open WebSocketConnection connection) {
		log.info("Game updates socket opened: {}", connection.id());
	}
	
	@OnTextMessage
	String process(String message) {
		if (connection.userData().get(UserData.TypedKey.forBoolean("isCool"))) {
			return "Cool message processed!";
		} else {
			return "Message processed!";
		}
	}
	
}
