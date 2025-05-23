<?php

namespace Ebprod\OqmCoreDepot;

use Monolog\Handler\SyslogHandler;
use Monolog\Level;
use Monolog\Logger;
use Monolog\Handler\StreamHandler;

class LogUtils {
	
	public static function getLogger(string $name):Logger{
		$log = new Logger($_SERVER['REQUEST_URI'] . ' / ' . $name);
		$log->pushHandler(new StreamHandler('php://stdout', Level::Debug));
		
		return $log;
	}
}