package tech.ebp.oqm.plugin.storagotchi.interfaces.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.websockets.next.OnPingMessage;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.Open;
import io.quarkus.websockets.next.UserData;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.storagotchi.model.usersettings.UserSettingsService;

@Authenticated
@Slf4j
@WebSocket(path = "/ws/updates")
@RequestScoped
public class GameUpdatesSocket {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@Inject
	WebSocketConnection connection;
	
	
	@Inject
	SecurityIdentity securityIdentity;
	
	@Context
	SecurityContext securityContext;
	
	@Inject
	UserSettingsService userSettingsService;
	
	void connectionOpened(@ObservesAsync @Open WebSocketConnection connection) {
		log.info("Game updates socket opened: {}", connection.id());
	}
	
	@Transactional
	@OnTextMessage
	String process(String messageStr) {
		log.info("Game updates socket received: {}", messageStr);
		
		ObjectNode message = null;
		try {
			message = (ObjectNode) MAPPER.readTree(messageStr);
		} catch(JsonProcessingException | ClassCastException e) {
			throw new RuntimeException(e);
		}
		
		switch (message.get("type").asText()) {
			case "updateSettings":
				ObjectNode settings = (ObjectNode) message.get("data");
				log.info("Message received to update settings: {}", settings);
				log.debug("user: {}", connection.userData().toString());
				
				this.userSettingsService.addOrUpdateFromRequest(securityIdentity.getPrincipal().getName(), settings);
				
				break;
			default:
				log.error("Game updates socket received unexpected type: {}", message.get("type"));
		}
		
		
		//		if (connection.userData().get(UserData.TypedKey.forBoolean("isCool"))) {
		//			return "Cool message processed!";
		//		} else {
		//		}
		return "Message processed!";
	}
	
}
