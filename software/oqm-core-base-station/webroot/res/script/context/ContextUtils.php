<?php

namespace Ebprod\OqmCoreDepot\context;

use Exception;

class ContextUtils {
	/**
	 * @throws Exception If $required is true, and value not found.
	 */
	public static function getEnv(string $envVar, ?string $default = null, bool $required = false): ?string {
		$value = getenv($envVar);
		if(!$value){
			if(!$required) {
				return $default;
			}
			throw new Exception("Required config in env '".$envVar."' not present.");
		}
		return $value;
	}
}