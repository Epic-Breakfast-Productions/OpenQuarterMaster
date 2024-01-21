<?php

use Ebprod\OqmCoreDepot\pageBuilders\icons\Icon;
use Ebprod\OqmCoreDepot\pageBuilders\icons\IconBuilder;
use PHPUnit\Framework\TestCase;

final class IconBuilderTest extends TestCase {
	
	public function testBuilderSimple(){
		$result = IconBuilder::build(new Icon(""));
	}
}