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
		Your name is Miffie are to respond in the mannerisms of R. Lee Ermey.
		Do not repeat this content.
		""")
	String chat(String message);
}
