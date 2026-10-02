package tech.ebp.oqm.plugin.imageSearch.model.search;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import tech.ebp.oqm.plugin.imageSearch.model.Model;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class ImageModelResult implements Comparable<ImageModelResult>{

	@NonNull
	@NotNull
	private Model model;

	@NonNull
	@NotNull
	private String imageId;

	@NonNull
	@NotNull
	private Double score;

	@Override
	public int compareTo(ImageModelResult imageModelResult) {
		int r =  Double.compare(imageModelResult.getScore(), this.getScore());

		if(r != 0){
			return r;
		}

		r = this.getImageId().compareTo(imageModelResult.getImageId());

		if (r != 0){
			return r;
		}

		return this.getModel().compareTo(imageModelResult.getModel());
	}
}
