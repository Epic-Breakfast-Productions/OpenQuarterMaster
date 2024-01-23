<?php

namespace Ebprod\OqmCoreDepot\context;

use Ebprod\OqmCoreDepot\LogUtils;
use Monolog\Logger;

final class Context {
	private static ?Logger $log = null;
	private static ?Context $INSTANCE = null;
	
	private static function log():Logger{
		if(self::$log == null){
			self::$log = LogUtils::getLogger(self::class);
		}
		return self::$log;
	}
	
	public static function instance():Context{
		if(self::$INSTANCE == null){
			self::$INSTANCE = new Context();
			self::log()->debug("Initted Context");
		}
		return self::$INSTANCE;
	}
	
	public $entriesDir = "/etc/oqm/ui.d/";
	public ?string $depotUrl;
	private string $bsVersion;
	private ?RunByContext $runBy = null;
	private ?OidcContext $oidc = null;
	
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
	
	public function runBy(): RunByContext {
		if($this->runBy == null){
			$this->runBy = new RunByContext();
			self::log()->debug("Initted RunBy context");
		}
		return $this->runBy;
	}
	
	public function getOidc(): OidcContext {
		if($this->oidc == null){
			$this->oidc = new OidcContext();
			self::log()->debug("Initted Oidc context");
		}
		return $this->oidc;
	}
}