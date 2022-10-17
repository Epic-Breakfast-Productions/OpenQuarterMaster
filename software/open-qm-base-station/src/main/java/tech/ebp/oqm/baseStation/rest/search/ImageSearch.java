package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.media.Image;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;

@ToString(callSuper = true)
@Getter
public class ImageSearch extends SearchKeyAttObject<Image> {
	@QueryParam("title") String imageTitle;
	//TODO:: object specific fields, add to bson filter list
	
	
	@HeaderParam("accept") String acceptHeaderVal;
	@HeaderParam("actionType") String actionTypeHeaderVal;
	@HeaderParam("searchFormId") String searchFormIdHeaderVal;
	@HeaderParam("inputIdPrepend") String inputIdPrependHeaderVal;
	@HeaderParam("otherModalId") String otherModalIdHeaderVal;
	
	@Override
	public int getDefaultPageSize() {
		return 36;
	}
}
