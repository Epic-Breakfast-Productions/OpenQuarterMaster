package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.baseStation.interfaces.RestInterface;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.SearchObject;

import java.util.*;


@Slf4j
public abstract class UiProvider extends RestInterface {

	@Getter
	@HeaderParam("x-forwarded-prefix")
	Optional<String> forwardedPrefix;
	
	@Inject
	Span span;

	protected String getRootPrefix(){
		return this.forwardedPrefix.orElse("");
	}
	
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
				   .data("rootPrefix", this.getRootPrefix())
				   .data("userInfo", this.getUserInfo())
				   .data("userToken", this.getUserTokenStr())
				   .data("oqmDbs", this.getOqmDatabases())
				   .data("selectedOqmDb", this.getSelectedDb())
				   .data("traceId", this.span.getSpanContext().getTraceId())
				   ;
	}
	
	protected TemplateInstance setupPageTemplate() {
		return this.setupPageTemplate(this.getPageTemplate());
	}
	
	protected Uni<Response> getUni(TemplateInstance pageTemplate, Map<String, Uni> uniMap) {
		Uni<Object> userInfoUni = this.getOqmCoreApiClient().interactingEntityGetSelf(this.getBearerHeaderStr())
										.map((ObjectNode userInfoJs)->{
											return getUserInfo().setId(userInfoJs.get("id").toString().replaceAll("\"", ""));
										});
		if(uniMap.isEmpty()){
			return userInfoUni.map((info)->{
				return Response.ok(
					pageTemplate,
					MediaType.TEXT_HTML_TYPE
				).build();
			});
		}
		TreeSet<String> keys = new TreeSet<>(uniMap.keySet());
		
		UniJoin.Builder<Object> uniJoinBuilder = Uni.join().builder();
		
		
		for(String key : keys){
			uniJoinBuilder.add(uniMap.get(key));
		}
		// add after others, to ensure we get it done.
		uniJoinBuilder.add(userInfoUni);
		
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
	protected Uni<Response> getUni() {
		return this.getUni(
			Map.of()
		);
	}
	
	@FunctionalInterface
	public interface ObjGetMethod {
		public Uni<ObjectNode> get(String one, String two, String three);
	}
	
}
