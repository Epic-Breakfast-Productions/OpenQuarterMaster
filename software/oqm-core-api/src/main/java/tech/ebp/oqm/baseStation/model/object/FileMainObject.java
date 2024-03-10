package tech.ebp.oqm.baseStation.model.object;

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
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileMainObject
	extends AttKeywordMainObject
	//	implements AttKeywordContaining
{
	
	public FileMainObject(ObjectId id, Map<@NotBlank @NotNull String, String> attributes, List<@NotBlank String> keywords) {
		super(id, attributes, keywords);
	}
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getGridfsFileName(){
		return this.getId().toHexString();
	}
	
	@NotNull
	@NonNull
	@Size(max = 500)
	private String description = "";
	
	@NotNull
	@NonNull
	@NotBlank
	@Size(max = 500)
	private String source = "user";
	
	public boolean updateFrom(FileUploadBody newUpload){
		//TODO:: update to return true only when actually updated
		if(newUpload.description != null && !newUpload.description.isBlank()){
			this.description = newUpload.description;
		}
		if(newUpload.source != null && !newUpload.source.isBlank()){
			this.source = newUpload.source;
		}
		return true;
	}
}
