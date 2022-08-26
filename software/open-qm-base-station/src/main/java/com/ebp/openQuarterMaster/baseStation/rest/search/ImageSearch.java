package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.lib.core.object.media.Image;
import lombok.Getter;
import lombok.ToString;

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
}
