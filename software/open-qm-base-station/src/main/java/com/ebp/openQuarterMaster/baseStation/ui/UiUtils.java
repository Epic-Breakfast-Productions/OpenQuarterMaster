package com.ebp.openQuarterMaster.baseStation.ui;

import org.eclipse.microprofile.config.ConfigProvider;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

public class UiUtils {
    public static NewCookie getAuthRemovalCookie(){
        return new NewCookie(
                new Cookie(
                        ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class),
                        "",
                        "/",
                        ConfigProvider.getConfig().getValue("runningInfo.hostname", String.class)
                ),
                "To clear out the auth cookie",
                0,
                false
        );
    }
}
