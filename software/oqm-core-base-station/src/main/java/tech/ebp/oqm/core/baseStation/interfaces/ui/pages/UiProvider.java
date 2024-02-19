package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.core.baseStation.interfaces.RestInterface;

import java.util.Currency;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


@Slf4j
public abstract class UiProvider extends RestInterface {
	
	@Inject
	Span span;
	
	protected abstract Template getPageTemplate();
	
	protected TemplateInstance setupPageTemplate(Template template) {
		return template
				   .data("userInfo", this.getUserInfo())
				   .data("traceId", this.span.getSpanContext().getTraceId())
				   .data("currency", ConfigProvider.getConfig().getValue("service.ops.currency", Currency.class))
				   ;
	}
	
	protected Uni<Response> getUni(TemplateInstance pageTemplate, Map<String, Uni> uniMap) {
		TreeSet<String> keys = new TreeSet<>(uniMap.keySet());
		
		UniJoin.Builder<Object> uniJoinBuilder = Uni.join().builder();
		
		for(String key : keys){
			uniJoinBuilder.add(uniMap.get(key));
		}
		
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map(resultList->{
					   {
						   Iterator<String> keyIt = keys.iterator();
						   Iterator<Object> resultIt = resultList.iterator();
						   
						   while(keyIt.hasNext() && resultIt.hasNext()){
							   String key = keyIt.next();
							   Object val = resultIt.next();
							   log.debug("Adding data from uni \"{}\": {}", key, val);
							   pageTemplate.data(key, val);
						   }
					   }
					   
					   return Response.ok(
						   pageTemplate,
						   MediaType.TEXT_HTML_TYPE
					   ).build();
				   });
	}
	
	protected Uni<Response> getUni(Map<String, Uni> uniMap) {
		return this.getUni(
			this.setupPageTemplate(this.getPageTemplate()),
			uniMap
		);
	}
}
