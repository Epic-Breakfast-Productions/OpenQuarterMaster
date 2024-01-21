<?php

namespace Ebprod\OqmCoreDepot;

use Monolog\Level;
use Monolog\Logger;
use Monolog\Handler\StreamHandler;

final class Context {
	private static Logger $log;
	private static ?Context $INSTANCE = null;
	
	public static function instance():Context{
		if(self::$INSTANCE == null){
			self::$INSTANCE = new Context();
			self::$log = LogUtils::getLogger(self::class);
			self::$log->debug("Initted Context.");
		}
		return self::$INSTANCE;
	}
	
	public $entriesDir = "/etc/oqm/ui.d/";
	
	public ?string $depotUrl;
	
	private function __construct() {
		$this->depotUrl = getenv("CFG_DEPOT_URL");
		
	}
	
	public function getEntriesDir(): string {
		return $this->entriesDir;
	}
	
	public function getDepotUrl(): ?string {
		return $this->depotUrl;
	}
	
	public function hasDepotUrl():bool {
		return is_null($this->getDepotUrl());
	}
}