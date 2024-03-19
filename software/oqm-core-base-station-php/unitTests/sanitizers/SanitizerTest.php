<?php

use Ebprod\OqmCoreDepot\sanitizers\Sanitizer;
use PHPUnit\Framework\TestCase;

final class SanitizerTest extends TestCase {
	
	public function testAllHtml(){
		$result = Sanitizer::allHtml("<script>alert('bad');</script><i></i><b></b><strong></strong><p></p>");
		echo "\n\nSanitized string (all): " . $result;
		self::assertStringNotContainsString("<script>", $result);
		self::assertStringNotContainsString("<i>", $result);
		self::assertStringNotContainsString("<b>", $result);
		self::assertStringNotContainsString("<strong>", $result);
		self::assertStringNotContainsString("<p>", $result);
	}
	public function testSimpleHtml(){
		$result = Sanitizer::simpleHtml("<script>alert('bad');</script><i></i><b></b><strong></strong><p>hello</p>");
		echo "\n\nSanitized string (simple): " . $result;
		self::assertStringNotContainsString("<script>", $result);
		self::assertStringNotContainsString("<p>", $result);
		self::assertStringContainsString("<i>", $result);
		self::assertStringContainsString("<b>", $result);
		self::assertStringContainsString("<strong>", $result);
	}
}