package tech.ebp.oqm.core.baseStation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.commons.text.WordUtils;

@Named("EnumFormattingService")
@ApplicationScoped
public class EnumFormattingService {

	public String formatEnum(Enum<?> enumInstance){
		return this.formatEnum(enumInstance.name());
	}

	public String formatEnum(String enumName){
		return
			WordUtils.capitalizeFully(
				enumName
					.replaceAll("_", " ")
					.replaceAll("-", " ")
					.toLowerCase()
			);
	}
}
