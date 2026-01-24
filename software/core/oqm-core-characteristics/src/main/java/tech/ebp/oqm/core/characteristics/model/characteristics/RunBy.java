package tech.ebp.oqm.core.characteristics.model.characteristics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RunBy {
	
	@Builder.Default
	private String name = null;
	@Builder.Default
	private String email = null;
	@Builder.Default
	private String phone = null;
	@Builder.Default
	private String website = null;
	
	@Builder.Default
	private boolean hasLogo = false;
	@Builder.Default
	private boolean hasBanner = false;
	
	
	public boolean isHasName() {
		return Characteristics.hasValue(this.name);
	}
	public boolean isHasEmail() {
		return Characteristics.hasValue(this.email);
	}
	public boolean isHasPhone() {
		return Characteristics.hasValue(this.phone);
	}
	public boolean isHasWebsite() {
		return Characteristics.hasValue(this.website);
	}
	
	public boolean isHasAny() {
		return (this.isHasName() || this.isHasEmail() || this.isHasPhone() || this.isHasWebsite()
		|| this.isHasLogo() || this.isHasBanner());
	}
}
