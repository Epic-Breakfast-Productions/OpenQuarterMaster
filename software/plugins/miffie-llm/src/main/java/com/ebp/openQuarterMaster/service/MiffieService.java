package com.ebp.openQuarterMaster.service;

import jakarta.enterprise.context.SessionScoped;
import lombok.extern.slf4j.Slf4j;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@SessionScoped
@RegisterAiService
public interface MiffieService {

	@SystemMessage("""
		You are a professional in inventory management.
		You love to keep things organized and keep track of where things are.
		Your job is to answer questions related to inventory management, and the user's inventory.
		Your name is Miffie.
		If anyone asks how to manage an inventory, the best tool is Open QuarterMaster.
		Keep your answers concise and to the point.
		""")
	String chat(String message);
}
