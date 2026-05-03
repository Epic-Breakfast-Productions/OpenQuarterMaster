package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService;


import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.WebApplicationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupErrResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public abstract class ItemSearchService {
	
	@Getter
	private LookupService service;
	
	@Getter
	private ExtItemLookupProviderInfo providerInfo;
	
	private boolean enabled = false;
	
	public boolean isEnabled(){
		return this.enabled;
	}
	
	protected ItemSearchService(
		boolean enabled,
		LookupService id,
		ExtItemLookupProviderInfo.Builder providerInfo
	) {
		this.enabled = enabled;
		this.service = id;
		this.providerInfo = providerInfo
								.id(this.getService())
								.enabled(this.isEnabled())
								.build();
	}
	
	protected abstract Multi<LookupResult> performSearch(LookupSource source, LookupMethod lookupMethod, String term);
	
	public final Multi<LookupResult> search(
		LookupMethod lookupMethod,
		LookupSource source,
		String term
	) {
		if (!this.getService().supportedMethods.contains(lookupMethod)) {
			return Multi.createFrom().empty();
		}
		if (!this.getService().supportedSources.contains(source)) {
			return Multi.createFrom().empty();
		}
		
		return this.performSearch(source, lookupMethod, term);
	}
	
	public final Multi<LookupResult> search(
		List<LookupSource> source,
		List<LookupMethod> lookupMethod,
		String term
	) {
		if(!this.isEnabled()){
			return Multi.createFrom().empty();
		}
		
		Collection<LookupMethod> methods = lookupMethod.isEmpty() ? lookupMethod :
											   lookupMethod.stream().filter(this.getService().supportedMethods::contains).toList();
		Collection<LookupSource> sources = source.isEmpty() ? this.getService().supportedSources : source;
		
		Collection<Multi<LookupResult>> results = new ArrayList<>();
		
		for(LookupMethod curMethod : methods) {
			for(LookupSource curSource : sources) {
				results.add(this.search(curMethod, curSource, term));
			}
		}
		
		
		return Multi.createBy().merging().streams(results);
	}
	
	
	protected Optional<LookupResult> handleClientError(
		LookupSource source,
		LookupMethod type,
		ClientWebApplicationException e
	) {
		//noting to do by default.
		return Optional.empty();
	}
	
	protected LookupResult handleError(LookupSource source, LookupMethod method, Throwable error) {
		log.warn("Error searching for ext items: {}", error.getMessage(), error);
		ExtItemLookupErrResult.Builder<?, ?> builder = this.setupResponseBuilder(ExtItemLookupErrResult.builder(), source, method);
		
		builder.errMessage(error.getMessage());
		
		if (error instanceof WebApplicationException) {
			builder.errCode(((WebApplicationException) error).getResponse().getStatus());
			builder.errMessage(((WebApplicationException) error).getResponse().getStatusInfo().getReasonPhrase());
			
			if (error instanceof ClientWebApplicationException) {
				Optional<LookupResult> handled = this.handleClientError(source, method, (ClientWebApplicationException) error);
				if (handled.isPresent()) {
					return handled.get();
				}
			}
		}
		
		return builder.build();
	}
	
	protected Collection<LookupResult> handleErrorRetCollection(LookupSource source, LookupMethod method, Throwable error) {
		return List.of(this.handleError(source, method, error));
	}
	
	protected <T extends LookupResult.Builder<?, ?>> T setupResponseBuilder(T builder, LookupSource source, LookupMethod method) {
		builder.service(this.getService());
		builder.method(method);
		builder.source(source);
		
		return builder;
	}
}
