package tech.ebp.oqm.lib.core.rest.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request to create an image in the Base Station service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageCreateRequest {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 50)
	public String title;
	
	@Size(max = 500)
	public String description = "";
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 50)
	public String source = "user";
	
	@NonNull
	@NotNull
	@NotBlank
	@Pattern(regexp = "^(data:image/.+;base64,)?(?:[A-Za-z\\d+/]{4})*(?:[A-Za-z\\d+/]{3}=|[A-Za-z\\d+/]{2}==)?$")
	public String imageData;
	
	@NonNull
	@NotNull
	public List<@NotNull String> keywords = new ArrayList<>();
	@NonNull
	@NotNull
	public Map<@NotNull @NotBlank String, @NotNull String> attributes = new HashMap<>();
}
