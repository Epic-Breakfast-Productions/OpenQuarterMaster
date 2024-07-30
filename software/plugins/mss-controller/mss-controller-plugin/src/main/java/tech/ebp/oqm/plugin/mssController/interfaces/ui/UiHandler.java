package tech.ebp.oqm.plugin.mssController.interfaces.ui;

import tech.ebp.oqm.plugin.mssController.interfaces.RestInterface;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
