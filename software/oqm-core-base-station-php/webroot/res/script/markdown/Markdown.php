<?php

namespace Ebprod\OqmCoreDepot\markdown;

use Ebprod\OqmCoreDepot\sanitizers\Sanitizer;
use Parsedown;

class Markdown {
	private static ?Parsedown $parsedown = null;
	
	public static function getParsedown():Parsedown{
		if(self::$parsedown == null){
			self::$parsedown = new Parsedown();
			self::$parsedown->setSafeMode(true);
		}
		return self::$parsedown;
	}
	
	private static function parseMd(string $data){
		return self::getParsedown()->parse($data);
	}
	public static function parseMdSimple(string $data){
		return Sanitizer::simpleHtml(self::parseMd($data));
	}
	public static function parseMdFull(string $data){
		return Sanitizer::markdownHtml(self::parseMd($data));
	}
}