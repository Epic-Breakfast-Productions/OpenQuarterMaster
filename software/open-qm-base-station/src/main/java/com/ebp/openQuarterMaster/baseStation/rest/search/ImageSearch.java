package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.lib.core.media.Image;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class ImageSearch extends SearchKeyAttObject<Image> {
	//TODO:: object specific fields, add to bson filter list
}
