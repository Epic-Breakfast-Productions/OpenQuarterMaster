package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.lib.core.user.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TestUserService {

    @Inject
    UserService userService;

    public static User getTestUser(boolean persisted) {
        return new User();
    }

    public static User getTestUser() {
        return new User();
    }
}
