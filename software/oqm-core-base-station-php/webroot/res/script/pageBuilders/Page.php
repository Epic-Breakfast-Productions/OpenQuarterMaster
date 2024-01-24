<?php

namespace Ebprod\OqmCoreDepot\pageBuilders;

use Ebprod\OqmCoreDepot\LogUtils;
use Ebprod\OqmCoreDepot\pageBuilders\icons\Icon;
use Monolog\Logger;

class Page {
	private static ?Logger $log = null;
	private static ?array $PAGES = null;
	
	public static function getPages(): array {
		if (self::$PAGES == null) {
			self::$PAGES = [
				"/overview.html" => new Page("/overview.html", Icon::$overviewPage, "Overview"),
				"/storageBlocks.html" => new Page("/storageBlocks.html", Icon::$storageBlocksPage, "Storage"),
			];
		}
		return self::$PAGES;
	}
	public static function log(): Logger {
		if (self::$log == null) {
			self::$log = LogUtils::getLogger(self::class);
		}
		return self::$log;
	}
	
	public static function getPage(?string $page = null): Page {
		if ($page == null) {
			$page = $_SERVER['SCRIPT_NAME'];
		}
		return self::getPages()[$page];
	}
	
	private string $path;
	private Icon $icon;
	private string $title;
	
	/**
	 * @param string $path
	 * @param Icon   $icon
	 */
	public function __construct(string $path, Icon $icon, string $title) {
		$this->path = $path;
		$this->icon = $icon;
		$this->title = $title;
		
	}
	
	public function getPath(): string {
		return $this->path;
	}
	
	public function getIcon(): Icon {
		return $this->icon;
	}
	
	public function getTitle(): string {
		return $this->title;
	}
	
	public function isCurrentPage():bool {
		return strcmp($_SERVER['SCRIPT_NAME'], $this->getPath()) == 0;
	}
}
