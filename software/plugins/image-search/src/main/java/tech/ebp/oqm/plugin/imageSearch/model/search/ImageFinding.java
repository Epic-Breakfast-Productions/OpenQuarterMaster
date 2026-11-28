package tech.ebp.oqm.plugin.imageSearch.model.search;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import tech.ebp.oqm.plugin.imageSearch.model.Model;

/**
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageFinding {

	@NotNull
	@NonNull
	String itemId;

	@NotNull
	@NonNull
	String imageId;

	@NotNull
	@NonNull
	Model model;

	@NotNull
	@NonNull
	Double score;

	public ImageModelResult toImageModel(){
		return new ImageModelResult(this.model, this.imageId, this.getScore());
	}
}
