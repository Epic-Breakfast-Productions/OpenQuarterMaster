<?php

namespace Ebprod\OqmCoreDepot\context;

class ContextUtils {
	public static function getEnv(string $envVar, ?string $default = null, bool $required = false): ?string {
		$value = getenv($envVar);
		if(!$value){
			return $default;
		}
		return $value;
	}
}