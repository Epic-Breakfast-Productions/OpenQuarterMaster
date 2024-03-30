<?php

namespace Ebprod\OqmCoreDepot\context;

use Ebprod\OqmCoreDepot\markdown\Markdown;
use Ebprod\OqmCoreDepot\sanitizers\Sanitizer;

class OidcContext {
	private string $host;
	private string $clientId;
	private string $clientSecret;
	
	public function __construct() {
		$this->host = Sanitizer::allHtml(ContextUtils::getEnv("CFG_OIDC_HOST", required: true));
		$this->clientId = Sanitizer::allHtml(ContextUtils::getEnv("CFG_OIDC_CLIENT_ID", required: true));
		$this->clientSecret = Sanitizer::allHtml(ContextUtils::getEnv("CFG_OIDC_CLIENT_SECRET", required: true));
	}
	
	public function getHost(): ?string {
		return $this->host;
	}
	
	public function getClientId(): ?string {
		return $this->clientId;
	}
	
	public function getClientSecret(): ?string {
		return $this->clientSecret;
	}
}