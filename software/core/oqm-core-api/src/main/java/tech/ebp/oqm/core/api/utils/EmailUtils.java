package tech.ebp.oqm.core.api.utils;

import io.quarkus.mailer.MailTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

@Slf4j
@ApplicationScoped
public class EmailUtils {
	
	@ConfigProperty(name = "runningInfo.fromEmail")
	String fromEmail;
	
	public MailTemplate.MailTemplateInstance setupDefaultEmailData(
		MailTemplate template,
		InteractingEntity entityTo,
		String subject
	) {
		//TODO:: add logo image data
		return template
				   .to(entityTo.getEmail())
				   .from(this.fromEmail)
				   .subject(subject + " - Open QuarterMaster")
				   .data("user", entityTo)
				   .data("subject", subject);
	}
	
}
