package tech.ebp.oqm.core.baseStation.interfaces.ui;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.core.baseStation.interfaces.RestInterface;

import java.time.ZonedDateTime;
import java.util.Currency;


@Slf4j
public abstract class UiProvider extends RestInterface {
	
	@Inject
	Span span;
	
	protected TemplateInstance setupPageTemplate(Template template) {
		return template
				   .data("userInfo", this.getUserInfo())
				   .data("traceId", this.span.getSpanContext().getTraceId())
				   .data("currency", ConfigProvider.getConfig().getValue("service.ops.currency", Currency.class))
				   .data("generateDatetime", ZonedDateTime.now());
	}
}
