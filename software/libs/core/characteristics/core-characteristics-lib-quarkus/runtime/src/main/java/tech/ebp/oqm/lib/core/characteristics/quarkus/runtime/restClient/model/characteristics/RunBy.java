package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.characteristics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RunBy {
	private String name;
	private String email;
	private String phone;
	private String website;
	private boolean hasLogoImg;
	private boolean hasBannerImg;
	
	public boolean hasName(){
		return this.name != null && !this.name.isBlank();
	}
	
	public boolean hasEmail(){
		return this.email != null && !this.email.isBlank();
	}
	
	public boolean hasPhone(){
		return this.phone != null && !this.phone.isBlank();
	}
	
	public boolean hasWebsite(){
		return this.website != null && !this.website.isBlank();
	}
	
	public boolean hasValues(){
		return this.hasName() || this.hasEmail() || this.hasPhone() || this.hasWebsite();
	}
	
	public boolean hasContactInfo(){
		return this.hasEmail() || this.hasPhone();
	}
	
}
