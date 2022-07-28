package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class UserSearch extends SearchKeyAttObject<User> {
	//TODO:: object specific fields, add to bson filter list
}
