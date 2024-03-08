package tech.ebp.oqm.baseStation.rest.search;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;
import tech.ebp.oqm.baseStation.model.object.media.Image;

import java.util.List;

@ToString(callSuper = true)
@Getter
public class ImageSearch extends FileSearchObject<Image> {
	@QueryParam("title") String imageTitle;
	@QueryParam("source") String imageSource;
	//TODO:: object specific fields, add to bson filter list
	
	@Override
	public List<Bson> getSearchFilters() {
		List<Bson> output = super.getSearchFilters();
		
		if(this.imageSource != null && !this.imageSource.isBlank()){
			output.add(Filters.eq("source", this.imageSource));
		}
		
		return output;
	}
}
