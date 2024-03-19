package tech.ebp.oqm.baseStation.model.rest.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
	@Size(max = 100)
	public String title;
	
	@Size(max = 500)
	public String description = "";
	
	@NonNull
	@NotNull
	@NotBlank
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
