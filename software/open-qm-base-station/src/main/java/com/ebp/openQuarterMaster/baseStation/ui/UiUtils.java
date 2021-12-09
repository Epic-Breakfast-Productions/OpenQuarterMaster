package com.ebp.openQuarterMaster.baseStation.ui;

import org.eclipse.microprofile.config.ConfigProvider;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

public class UiUtils {
    public static final int DEFAULT_COOKIE_AGE = 86400; //1 day

    public static NewCookie getNewCookie(
            String cookieName,
            String value,
            String comment,
            int maxAgeSecs
    ){
        return new NewCookie(
                new Cookie(
                        cookieName,
                        value,
                        "/",
                        ConfigProvider.getConfig().getValue("runningInfo.hostname", String.class)
                ),
                comment,
                maxAgeSecs,
                false
        );
    }

    public static NewCookie getRemovalCookie(String cookieName){
        return getNewCookie(
                cookieName,
                "",
                "To remove \"" + cookieName + "\" cookie.",
                0
        );
    }

    public static NewCookie getAuthRemovalCookie(){
        return getRemovalCookie(ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class));
    }

    public static NewCookie getAuthCookie(String jwt, int ageMaxSecs){
        return getNewCookie(
                ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class),
                jwt,
                "JWT from externl auth.",
                ageMaxSecs
        );
    }
}
