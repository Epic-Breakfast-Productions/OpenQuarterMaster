<?php

namespace Ebprod\OqmCoreDepot\sanitizers;

use HTMLPurifier;
use HTMLPurifier_Config;

class Sanitizer {
	
	private static ?HTMLPurifier $simpleSanitizer = null;
	private static ?HTMLPurifier $markdownSanitizer = null;
	
	private static function getSimpleSanitizer():HTMLPurifier{
		if(self::$simpleSanitizer == null){
			$config = HTMLPurifier_Config::createDefault();
			$config->set('HTML.AllowedElements', 'i,b,strong');
			self::$simpleSanitizer = new HTMLPurifier($config);
		}
		return self::$simpleSanitizer;
	}
	
	private static function getMarkdownSanitizer():HTMLPurifier{
		if(self::$markdownSanitizer == null){
			$config = HTMLPurifier_Config::createDefault();
			$config->set('HTML.AllowedElements', 'a,i,b,p,div,strong,h1,h2,h3,h4,h5,ol,ul,li,br,table,tr,th,td');
			$config->set('HTML.AllowedAttributes', 'href,title,target,name,colspan,rowspan');
			self::$markdownSanitizer = new HTMLPurifier($config);
		}
		return self::$markdownSanitizer;
	}
	
	public static function allHtml(?string $data): ?string {
		if(is_null($data)){
			return null;
		}
		return htmlspecialchars($data);
	}
	
	public static function simpleHtml(?string $data): ?string {
		if(is_null($data)){
			return null;
		}
		return self::getSimpleSanitizer()->purify($data);
	}
	
	public static function markdownHtml(?string $data): ?string {
		if(is_null($data)){
			return null;
		}
		return self::getMarkdownSanitizer()->purify($data);
	}
	
}