package com.ebp.openQuarterMaster.lib.core.rest.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageCreateRequest {
	
	@NonNull
	@NotNull
	@NotBlank
	public String title;
	@NonNull
	@NotNull
	public String description = "";
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
	public Map<@NotNull String, @NotNull String> attributes = new HashMap<>();
}
