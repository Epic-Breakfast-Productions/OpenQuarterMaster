package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.characteristics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Characteristics {
	private String title;
	private String motd;
	private RunBy runBy;
	private Banner banner;
	
	public boolean hasRunBy(){
		return this.runBy != null;
	}
	
	public boolean hasBanner(){
		return this.banner != null;
	}
	
	public boolean hasTitle(){
		return this.title != null && !this.title.isBlank();
	}
	public boolean hasMotd(){
		return this.motd != null && !this.motd.isBlank();
	}
}
