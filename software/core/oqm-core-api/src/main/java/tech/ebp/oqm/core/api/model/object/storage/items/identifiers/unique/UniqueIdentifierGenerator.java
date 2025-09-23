package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.math.BigInteger;


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
	private String name = null;
	
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
	
	/**
	 * If this generator uses an incrementor or not.
	 * @return
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean hasIncrement(){
		return (this.lastIncremented != null);
	}
	
	/**
	 * If whether or not to base64 encode the resulting id string
	 */
	@lombok.Builder.Default
	private boolean encoded = false;
	
	/**
	 * If the resulting ids are intended to be barcodes.
	 */
	@lombok.Builder.Default
	private boolean barcode = false;
	
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
