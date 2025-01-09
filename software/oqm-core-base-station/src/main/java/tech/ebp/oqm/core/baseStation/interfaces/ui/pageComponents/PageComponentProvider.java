package tech.ebp.oqm.core.baseStation.interfaces.ui.pageComponents;

import io.opentelemetry.api.trace.Span;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.core.baseStation.interfaces.RestInterface;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;

import java.util.Currency;


@Slf4j
public abstract class PageComponentProvider extends ApiProvider {
	public static final String PAGE_COMPONENT_ROOT = API_ROOT + "/pageComponents";
}
