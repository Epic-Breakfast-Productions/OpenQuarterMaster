<?php
namespace Ebprod\OqmCoreDepot;
enum EntryCategory {
	case core;
	case plugin;
	case infra;
	case metric;
	
	public static function fromName(string $name): EntryCategory {
		foreach (self::cases() as $status) {
			if( $name === $status->name ){
				return $status;
			}
		}
		throw new \ValueError("$name is not a valid backing value for enum " . self::class );
	}
}