package tech.ebp.oqm.baseStation.utils;

import io.quarkus.mailer.MailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import jakarta.enterprise.context.ApplicationScoped;

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
