package tech.ebp.oqm.lib.core.object.storage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.HasParent;
import tech.ebp.oqm.lib.core.object.ImagedMainObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.awt.*;

/**
 * TODO:: color validator?
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class ItemCategory extends ImagedMainObject implements HasParent {
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 25)
	private String name;
	
	@NonNull
	@NotNull
	private String description = "";
	
	private Color color = null;
	
	private ObjectId parent = null;
}
