package tech.ebp.oqm.baseStation.data.sanitizers;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.utils.AuthMode;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerInfoBean {
	
	@ConfigProperty(name = "service.authMode")
	AuthMode authMode;
}
