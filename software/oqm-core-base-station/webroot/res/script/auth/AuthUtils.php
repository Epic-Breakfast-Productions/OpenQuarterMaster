<?php

namespace Ebprod\OqmCoreDepot\auth;

use Ebprod\OqmCoreDepot\context\Context;
use Jumbojett\OpenIDConnectClient;

/**
 * Refs:
 *  - https://github.com/jumbojett/OpenID-Connect-PHP
 */
class AuthUtils {
	private static ?OpenIDConnectClient $oidc = null;
	private static string $CLAIM_USERNAME = "username";
	
	private static function getOidc(): OpenIDConnectClient {
		if (self::$oidc == null) {
			self::$oidc = new OpenIDConnectClient(Context::instance()->getOidc()->getHost(),
				Context::instance()->getOidc()->getClientId(),
				Context::instance()->getOidc()->getClientSecret()
			);
		}
		return self::$oidc;
	}
	
	public static function ensureLoggedIn(): void {
		self::getOidc()->authenticate();
	}
	
	public static function getUsername(): string {
		return self::getOidc()->getVerifiedClaims(self::$CLAIM_USERNAME);
	}
	
	public static function getUsersToken(): string {
		return self::getOidc()->getAccessToken();
	}
	
}