package com.ebp.openQuarterMaster.plugin.interfaces.ui;

import com.ebp.openQuarterMaster.plugin.interfaces.RestInterface;
import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@NoArgsConstructor
public class UiHandler extends RestInterface {

	protected TemplateInstance setupTemplate(Template template){
		return template
			.data("username", this.getUserToken().getClaim("name"))
			.data("selectedOqmDb", this.getSelectedDb())
			.data("voiceSearchEnabled", false)
			;
	}
}
