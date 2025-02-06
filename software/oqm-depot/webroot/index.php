<?php

require __DIR__ . '/vendor/autoload.php';

use Ebprod\OqmCoreDepot\Context;
use Ebprod\OqmCoreDepot\EntryCategory;
use Ebprod\OqmCoreDepot\UiEntry;

//print "<h1>Hello, world!</h1>";
//phpinfo();
$depotVersion = "";
$numCorePlugins = 0;
$numInfraMetrics = 0;

$coreContent = "";
$pluginContent = "";
$metricsContent = "";
$infraContent = "";

{
	$jsonRaw = file_get_contents($_SERVER['DOCUMENT_ROOT'] . "/composer.json");
	//json_validate($jsonRaw);//needed?
	$json = json_decode($jsonRaw, true);
	$depotVersion = $json["version"];
}

$files = file_exists(Context::$ENTRIES_DIR);

if($files){
    $files = scandir(Context::$ENTRIES_DIR);
}

if ($files) {
	$entryArr = [];
	foreach ($files as $curFile) {
		if (!str_ends_with($curFile, ".json")) {
			continue;
		}
		$curEntry = new UiEntry(Context::$ENTRIES_DIR . $curFile);
		$entryArr[] = $curEntry;
	}
	
	foreach ($entryArr as $curEntry) {
		$curContent = '
<tr>
	<td><a href="' . $curEntry->getUrl() . '" target="_self" title="Go to ' . $curEntry->getName() . '">' . $curEntry->getName() . '</a></td>
	<td>' . $curEntry->getDescription() . '</td>
	<td></td>
</tr>
';
		switch ($curEntry->getType()) {
			case EntryCategory::core:
				$numCorePlugins++;
				$coreContent .= $curContent;
				break;
			case EntryCategory::plugin:
				$numCorePlugins++;
				$pluginContent .= $curContent;
				break;
			case EntryCategory::infra:
				$numInfraMetrics++;
				$infraContent .= $curContent;
				break;
			case EntryCategory::metric:
				$numInfraMetrics++;
				$metricsContent .= $curContent;
		}
	}
}

if ($coreContent == "") {
	$coreContent = "<tr><td colspan='3'>None Available</td></tr>";
}
if ($pluginContent == "") {
	$pluginContent = "<tr><td colspan='3'>None Available</td></tr>";
}
if ($infraContent == "") {
	$infraContent = "<tr><td colspan='3'>None Available</td></tr>";
}
if ($metricsContent == "") {
	$metricsContent = "<tr><td colspan='3'>None Available</td></tr>";
}

function getEntryTable($name, $content) {
	return '
<h3 class="mt-5">' . $name . '</h3>
<table class="table table-bordered table-hover table-striped table-sm">
<thead>
<tr>
	<th>
		Name & Link
	</th>
	<th>
		Description
	</th>
	<th>
		Option
	</th>
</tr>
</thead>
<tbody>
	' . $content . '
</tbody>
</table>
';
}

?>
<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="shortcut icon" href="/favicon.ico"/>
	<title>OQM Depot</title>
	<link href="<?=Context::getRootPath();?>/res/lib/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="min-vh-100 vstack">
<main class="container flex-grow-1">
	<div class="mb-3 mt-3 text-center">
		<img src="<?=Context::getRootPath();?>/res/media/logo.svg" class="" style="max-width: 75%;" alt="OQM Logo">
		<h1 class="display-5 fw-bold text-body-emphasis">OQM Depot</h1>
		<p class="lead mb-4">
			This is where you can access all your Open QuarterMaster components. Shown below are all available front-end interfaces you can visit and interact with.
		</p>
	</div>
	<ul class="nav nav-tabs" id="mainTab" role="tablist">
		<li class="nav-item" role="presentation">
			<button class="nav-link active" id="corePluginsTab" data-bs-toggle="tab" data-bs-target="#corePluginsPane" type="button" role="tab" aria-controls="corePluginsPane" aria-selected="true">
				Core & Plugins
				<span class="badge rounded-pill text-bg-dark"><?php echo $numCorePlugins; ?></span>
			</button>
		</li>
		<li class="nav-item" role="presentation">
			<button class="nav-link" id="metricsInfraTab" data-bs-toggle="tab" data-bs-target="#metricsInfraPane" type="button" role="tab" aria-controls="metricsInfraPane" aria-selected="false">
				Metrics & Infrastructure
				<span class="badge rounded-pill text-bg-dark"><?php echo $numInfraMetrics; ?></span>
			</button>
		</li>
	</ul>
	<div class="tab-content" id="mainTabContent">
		<div class="tab-pane fade show active" id="corePluginsPane" role="tabpanel" aria-labelledby="corePluginsTab" tabindex="0">
			<h2 class="mt-2">
				Core components & Plugins
			</h2>
			<p>
				These components form the interfaces in which you interact with the system.
			</p>
			<?php echo getEntryTable("Core Components", $coreContent); ?>
			<?php echo getEntryTable("Plugins", $pluginContent); ?>
		</div>
		<div class="tab-pane fade" id="metricsInfraPane" role="tabpanel" aria-labelledby="metricsInfraTab" tabindex="0">
			<h2 class="mt-2">
				Metrics & Infrastructure
			</h2>
			<p>
				Metrics keep track of how well things are running. Infrastructure components are on the backend helping the system run.
			</p>
			<?php echo getEntryTable("Metrics", $metricsContent); ?>
			<?php echo getEntryTable("Infrastructure", $infraContent); ?>
		</div>
	</div>
</main>
<footer id="footer" class="container mb-3" role="contentinfo">
	<hr/>
	<div class="row">
		<div class="col-sm-4">
			<span class="h5">Open QuarterMaster Depot</span><br/>
			Version
			<a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster" target="_blank"><?php echo $depotVersion; ?></a>, &copy; 2024
			<a href="https://epic-breakfast-productions.tech/" target="_blank">EBP
				<img src="<?=Context::getRootPath();?>/res/media/EBP-logo-icon.svg" style="max-height:1.2em;" alt="EBP Logo"/></a><br/>
			Released under the
			<a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/LICENSE" target="_blank">GPL v3.0 License</a><br/>
		</div>
	</div>
</footer>

<script src="<?=Context::getRootPath();?>/res/lib/jquery/3.7.1/jquery.min.js.js"></script>
<script src="<?=Context::getRootPath();?>/res/lib/bootstrap/5.3.2/js/bootstrap.bundle.js"></script>

<?php
//print_r($_SERVER);
//print_r($_ENV);
//print_r($_REQUEST);
//print_r($_SESSION);
//print_r(headers_list());

?>
</body>
</html>
