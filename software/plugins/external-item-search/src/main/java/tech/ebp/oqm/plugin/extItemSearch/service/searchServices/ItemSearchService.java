package tech.ebp.oqm.plugin.extItemSearch.service.searchServices;


import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupErrResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class ItemSearchService {
	
	public abstract ExtItemLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
	
	public Optional<Multi<LookupResult>> searchName(String search) {
		return Optional.empty();
	}
	
	public Optional<Multi<LookupResult>> searchUrl(String search) {
		return Optional.empty();
	}
	
	public Optional<Multi<LookupResult>> searchBarcode(String search) {
		return Optional.empty();
	}
	
	public Optional<Multi<LookupResult>> searchPartNum(String search) {
		return Optional.empty();
	}
	
	public Multi<LookupResult> freeSearch(String search) {
		List<Optional<Multi<LookupResult>>> resultUniOps = new ArrayList<>(LookupType.values().length);
		
		for (LookupType type : LookupType.values()) {
			switch (type) {
				case NAME -> resultUniOps.add(this.searchName(search));
				case BARCODE -> resultUniOps.add(this.searchBarcode(search));
				case PART_NUM -> resultUniOps.add(this.searchPartNum(search));
				case URL -> resultUniOps.add(this.searchUrl(search));
			}
		}
		
		List<Multi<LookupResult>> resultUnis = resultUniOps.stream()
															 .filter(Optional::isPresent)
															 .map(Optional::get)
															 .toList();
		
		return Multi.createBy().merging().streams(resultUnis);
	}
	
	public Optional<Multi<LookupResult>> search(LookupType type, String search) {
		return switch (type) {
			case FREE_TEXT -> Optional.of(this.freeSearch(search));
			case URL -> this.searchUrl(search);
			case NAME -> this.searchName(search);
			case BARCODE -> this.searchBarcode(search);
			case PART_NUM -> this.searchPartNum(search);
		};
	}
	
	protected Optional<LookupResult> handleClientError(
		LookupType type,
		ClientWebApplicationException e
	) {
		//noting to do by default.
		return Optional.empty();
	}
	
	protected LookupResult handleError(LookupType type, Throwable error) {
		log.warn("Error searching for ext items: {}", error.getMessage(), error);
		ExtItemLookupErrResult.Builder<?, ?> builder = this.setupResponseBuilder(ExtItemLookupErrResult.builder(), type);
		
		builder.lookupType(type);
		builder.source(this.getProviderInfo().getId());
		
		builder.errMessage(error.getMessage());
		
		if(error instanceof WebApplicationException) {
			builder.errCode(((WebApplicationException) error).getResponse().getStatus());
			builder.errMessage(((WebApplicationException) error).getResponse().getStatusInfo().getReasonPhrase());
			
			if(error instanceof ClientWebApplicationException){
				Optional<LookupResult> handled = this.handleClientError(type, (ClientWebApplicationException) error);
				if(handled.isPresent()){
					return handled.get();
				}
			}
		}
		
		return builder.build();
	}
	
	protected Collection<LookupResult> handleErrorRetCollection(LookupType type, Throwable error) {
		return List.of(this.handleError(type, error));
	}
	
	protected <T extends LookupResult.Builder<?,?>> T setupResponseBuilder(T builder, LookupType type) {
		builder.lookupType(type);
		builder.source(this.getProviderInfo().getId());
		
		return builder;
	}
}
