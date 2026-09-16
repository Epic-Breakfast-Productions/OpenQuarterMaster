package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.config.OqmCoreCharacteristicsConfig;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.OqmCoreCharacteristicsRestClient;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.AllInfo;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.uis.Ui;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.uis.Uis;

import java.io.FilterReader;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Named("OqmCoreCharacteristicsService")
@ApplicationScoped
public class OqmCoreCharacteristicsService {

	@Inject
	OqmCoreCharacteristicsConfig config;

	@RestClient
	OqmCoreCharacteristicsRestClient oqmCoreCharacteristicsClient;

	public boolean isEnabled() {
		String baseUri = this.config.baseUri();
		return baseUri != null && !baseUri.isBlank();
	}

	public Uni<ObjectNode> health() {
		return this.oqmCoreCharacteristicsClient.health();
	}

	public Uni<AllInfo> allInfo() {
		return this.oqmCoreCharacteristicsClient.allInfo();
	}

	public Uni<Response> characteristicsRunByLogo() {
		return this.oqmCoreCharacteristicsClient.characteristicsLogo();
	}

	public Uni<Response> characteristicsRunByBanner() {
		return this.oqmCoreCharacteristicsClient.characteristicsBanner();
	}

	public Uni<Response> getUiIcon(String category, String id) {
		return this.oqmCoreCharacteristicsClient.getUiIcon(category, id);
	}

	public List<Ui> getUisInCategory(Uis uis, String category) {
		return uis.getUiCategory(category)
				   .stream()
				   .filter(ui->!this.config.serviceId().equals(ui.getId()))
				   .toList();
	}

	public boolean haveUisInCategory(Uis uis, String category) {
		return !this.getUisInCategory(uis, category).isEmpty();
	}
}
