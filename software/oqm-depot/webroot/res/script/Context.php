<?php

namespace Ebprod\OqmCoreDepot;
class Context {
	public static $ENTRIES_DIR = "/etc/oqm/ui.d/";
	
	public static function getRootPath(): string {
		if(isset($_SERVER["HTTP_X_FORWARDED_PREFIX"])){
			return $_SERVER["HTTP_X_FORWARDED_PREFIX"];
		}
		return "";
	}
}