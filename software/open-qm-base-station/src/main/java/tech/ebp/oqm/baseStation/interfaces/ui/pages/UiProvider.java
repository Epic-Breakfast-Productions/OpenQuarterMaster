package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.baseStation.interfaces.RestInterface;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

import java.time.ZonedDateTime;
import java.util.Currency;


@Slf4j
public abstract class UiProvider extends RestInterface {
	
	protected static final String USER_INFO_DATA_KEY = "userInfo";
	
	protected TemplateInstance setupPageTemplate(Template template, Span span) {
		return template
				   .data("traceId", span.getSpanContext().getTraceId())
				   .data("currency", ConfigProvider.getConfig().getValue("service.ops.currency", Currency.class))
				   .data("generateDatetime", ZonedDateTime.now())
				   .data("dateTimeFormatter", UiUtils.DATE_TIME_FORMATTER);
	}
	
	protected TemplateInstance setupPageTemplate(Template template, Span span, InteractingEntity entity) {
		return this.setupPageTemplate(template, span).data(USER_INFO_DATA_KEY, entity);
	}
	
	protected TemplateInstance setupPageTemplate(
		Template template,
		Span span,
		InteractingEntity entity,
		SearchResult<?> searchResults
	) {
		return this.setupPageTemplate(template, span, entity)
				   .data("showSearch", searchResults.isHadSearchQuery())
				   .data("searchResult", searchResults)
				   .data("pagingCalculations", new PagingCalculations(searchResults));
	}
	
}
