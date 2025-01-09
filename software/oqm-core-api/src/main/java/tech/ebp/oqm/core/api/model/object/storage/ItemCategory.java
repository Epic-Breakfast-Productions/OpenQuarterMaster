package tech.ebp.oqm.core.api.model.object.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.HasParent;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;

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
	public static final int CUR_SCHEMA_VERSION = 1;
	
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
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public Color getTextColor(){
		// https://stackoverflow.com/a/35970186/3015723
		Color c = this.getColor();
		if(c == null){
			return Color.BLACK;
		}
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		return (r * 0.299 + g * 0.587 + b * 0.114) > 186
				   ? Color.BLACK
				   : Color.WHITE;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
