<?php

namespace Ebprod\OqmCoreDepot\pageBuilders;

use Ebprod\OqmCoreDepot\pageBuilders\icons\Icon;

class NavEntry {
	private static ?array $NAV_ENTRIES = null;
	
	public static function getNavEntries(): array {
		if (self::$NAV_ENTRIES == null) {
			self::$NAV_ENTRIES = [
				new NavEntry(Page::getPage("/overview.html")),
				new NavEntry(Page::getPage("/storageBlocks.html"))
			];
		}
		return self::$NAV_ENTRIES;
	}
	
	private Page $page;
	private array $subEntries;
	
	/**
	 * @param Page  $page
	 * @param array $subEntries
	 */
	public function __construct(Page $page, array $subEntries = []) {
		$this->page = $page;
		$this->subEntries = $subEntries;
	}
	
	public function getPage(): Page {
		return $this->page;
	}
	
	public function getSubEntries(): array {
		return $this->subEntries;
	}
	
	public function isActive(): bool {
		if($this->getPage()->isCurrentPage()){
			return true;
		}
		foreach ($this->getSubEntries() as $curSubEntry){
			if($curSubEntry->isActive()){
				return true;
			}
		}
		
		return false;
	}
	
}