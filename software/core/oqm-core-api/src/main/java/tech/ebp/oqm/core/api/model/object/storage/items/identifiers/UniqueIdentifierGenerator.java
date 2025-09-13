package tech.ebp.oqm.core.api.model.object.storage.items.identifiers;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.math.BigInteger;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UniqueIdentifierGenerator extends MainObject {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	/**
	 * The name of the generator
	 */
	@lombok.Builder.Default
	private String generatorName = null;
	
	/**
	 * The format of the id
	 */
	@NotNull
	@NonNull
	@NotBlank
	private String idFormat;
	
	/**
	 * The last generated increment
	 */
	@lombok.Builder.Default
	private BigInteger lastIncremented = null;
	
	public boolean hasIncrement(){
		return (this.lastIncremented != null);
	}
	
	/**
	 * If whether or not to base64 encode the resulting string
	 */
	@lombok.Builder.Default
	private boolean encoded = false;
	
	
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
