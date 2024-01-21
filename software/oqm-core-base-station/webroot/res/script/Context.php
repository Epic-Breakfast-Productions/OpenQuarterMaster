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
	private string $bsVersion;
	
	
	private function __construct() {
		$this->depotUrl = getenv("CFG_DEPOT_URL");
		
		$jsonRaw = file_get_contents($_SERVER['DOCUMENT_ROOT'] . "/composer.json");
		//json_validate($jsonRaw);//needed?
		$json = json_decode($jsonRaw, true);
		$this->bsVersion = $json["version"];
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
	
	public function getBsVersion(): string {
		return $this->bsVersion;
	}
}