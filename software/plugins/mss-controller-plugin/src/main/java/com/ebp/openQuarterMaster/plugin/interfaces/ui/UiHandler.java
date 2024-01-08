package com.ebp.openQuarterMaster.plugin.interfaces.ui;

import io.quarkus.oidc.IdToken;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@NoArgsConstructor
public class UiHandler {
	
	
	@Getter(AccessLevel.PROTECTED)
	@Inject
	@IdToken
	JsonWebToken idToken;
	
	@Getter(AccessLevel.PROTECTED)
	@Inject
	JsonWebToken accessToken;
	
	protected boolean hasIdToken() {
		return this.getIdToken() != null && this.getIdToken().getClaimNames() != null;
	}
	
	protected boolean hasAccessToken(){
		return this.getAccessToken() != null && this.getAccessToken().getClaimNames() != null;
	}
	
	/**
	 * When hit from bare API call with just bearer, token will be access token.
	 *
	 * When hit from ui, idToken.
	 * @return
	 */
	protected JsonWebToken getUserToken(){
		if(this.hasIdToken()){
			return this.getIdToken();
		}
		if(this.hasAccessToken()){
			return this.getAccessToken();
		}
		return null;
	}

}
