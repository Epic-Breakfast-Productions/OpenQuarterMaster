package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.SearchObject;

import java.util.Currency;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


@Slf4j
public abstract class UiProvider extends RestInterface {
	
	@Inject
	Span span;
	
	protected int getDefaultPageSize(){
		return 25;
	}
	
	protected void ensureSearchDefaults(SearchObject searchObject){
		if(searchObject.getPageNum() == null || searchObject.getPageNum() < 1){
			searchObject.setPageNum(1);
		}
		if(searchObject.getPageSize() == null || searchObject.getPageSize() < 1){
			searchObject.setPageSize(this.getDefaultPageSize());
		}
	}
	
	protected abstract Template getPageTemplate();
	
	protected TemplateInstance setupPageTemplate(Template template) {
		return template
				   .data("userInfo", this.getUserInfo())
				   .data("traceId", this.span.getSpanContext().getTraceId())
				   .data("currency", ConfigProvider.getConfig().getValue("service.ops.currency", Currency.class))
				   ;
	}
	
	protected TemplateInstance setupPageTemplate() {
		return this.setupPageTemplate(this.getPageTemplate());
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
			this.setupPageTemplate(),
			uniMap
		);
	}
	
	@FunctionalInterface
	public interface ObjGetMethod {
		public Uni<ObjectNode> get(String one, String two);
	}
	
	protected Uni<ObjectNode> addParentLabelsToSearchResults(ObjectNode results, String labelKey, ObjGetMethod parentGetCall){
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();
		
		//TODO:: do map thing to not call more than needed
		boolean hadParents = false;
		for(JsonNode curResult : (ArrayNode)results.get("results")){
			if(curResult.get("hasParent").asBoolean()){
				hadParents = true;
				uniJoinBuilder.add(
					parentGetCall.get(getBearerHeaderStr(), curResult.get("parent").asText())
						.invoke((ObjectNode storageBlock) ->{
							((ObjectNode)curResult).set("parentLabel", storageBlock.get(labelKey));
						})
				);
			}
		}
		if(!hadParents){
			return Uni.createFrom().item(results);
		}
		return uniJoinBuilder.joinAll()
				   .andCollectFailures()
				   .map((list)->{
					   return results;
				   });
	}
}