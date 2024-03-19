<?php

namespace Ebprod\OqmCoreDepot\auth;

use Ebprod\OqmCoreDepot\context\Context;
use Ebprod\OqmCoreDepot\LogUtils;
use Jumbojett\OpenIDConnectClient;
use Monolog\Logger;
use TypeError;

/**
 * Refs:
 *  - https://github.com/jumbojett/OpenID-Connect-PHP
 */
class AuthUtils {
	private static ?Logger $log = null;
	private static ?OpenIDConnectClient $oidc = null;
	private static string $CLAIM_USERNAME = "name";
	
	private static function log():Logger{
		if(self::$log == null){
			self::$log = LogUtils::getLogger(self::class);
		}
		return self::$log;
	}
	
	private static function getOidc(): OpenIDConnectClient {
		if (self::$oidc == null) {
			self::$oidc = new OpenIDConnectClient(Context::instance()->getOidc()->getHost(),
				Context::instance()->getOidc()->getClientId(),
				Context::instance()->getOidc()->getClientSecret()
			);
//			self::$oidc->setAllowImplicitFlow(true);
			
			self::$oidc->addScope(array('openid'));
			self::$oidc->setAllowImplicitFlow(true);
			self::$oidc->addAuthParam(array('response_mode' => 'form_post'));
			
//			self::log()->debug("Implicit flow enabled? ".(self::$oidc->getAllowImplicitFlow()?"true":"false"));
			if(Context::instance()->devModeEnabled()){
				self::$oidc->setHttpUpgradeInsecureRequests(false);
			}
		}
		return self::$oidc;
	}
	
	public static function ensureLoggedIn(): void {
		self::log()->info("Authenticating user.");
		
		try {
			self::getOidc()->getAccessToken();
			self::log()->info("Had access token.");
		} catch (TypeError $e){
			self::log()->info("Need to authenticate.");
			self::getOidc()->authenticate();
		}
		
		self::log()->info("Authenticated user: " . self::getUsername());
		self::log()->debug("User token: " . self::getUsersToken());
	}
	
	public static function getUsername(): string {
		self::log()->debug("Getting username.");
		return self::getOidc()->getVerifiedClaims(self::$CLAIM_USERNAME);
	}
	
	public static function getUsersToken(): string {
		return self::getOidc()->getAccessToken();
	}
	
}