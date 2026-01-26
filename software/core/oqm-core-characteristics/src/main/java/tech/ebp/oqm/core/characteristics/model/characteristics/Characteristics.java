package tech.ebp.oqm.core.characteristics.model.characteristics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;

@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Characteristics {
	public static boolean hasValue(String value){
		return (value != null && !value.isBlank());
	}
	public static boolean hasValue(Path value){
		return (value != null);
	}
	
	@Builder.Default
	private String title = null;
	
	@Builder.Default
	private String motd = null;
	
	@Builder.Default
	private RunBy runBy = null;
	
	@Builder.Default
	private Banner banner = null;
	
	public boolean isHasTitle() {
		return Characteristics.hasValue(this.title);
	}
	
	public boolean isHasRunBy() {
		return (this.runBy != null && this.runBy.isHasAny());
	}
	
	public boolean isHasMotd() {
		return Characteristics.hasValue(this.motd);
	}
	
	public boolean isHasBanner(){
		return (this.banner == null);
	}
}
