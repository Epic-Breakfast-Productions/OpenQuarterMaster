package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;

@ApplicationScoped
public class TestUserService {
    public static final String TEST_PASSWORD_ATT_KEY = "TEST_PASSWORD";

    @Inject
    UserService userService;

    @Inject
    PasswordService passwordService;

    Faker faker = Faker.instance();


    public User getTestUser(boolean admin, boolean persisted) {
        User.Builder builder = User.builder();

        builder.username(this.faker.name().username());
        builder.firstName(this.faker.name().firstName());
        builder.lastName(this.faker.name().lastName());
        builder.email(this.faker.internet().emailAddress());
        builder.title(this.faker.company().profession());
        builder.roles(new ArrayList<>() {{
            add("user");
            if (admin) {
                add("userAdmin");
            }
        }});

        String password = RandomStringUtils.random(16);
        builder.pwHash(this.passwordService.createPasswordHash(password));

        User user = builder.build();

        user.getAttributes().put(TEST_PASSWORD_ATT_KEY, password);

        if (persisted) {
            this.userService.add(user, null);
        }

        return user;
    }

    public User getTestUser(boolean admin) {
        return this.getTestUser(admin, true);
    }

    public User getTestUser() {
        return this.getTestUser(false, false);
    }
}
