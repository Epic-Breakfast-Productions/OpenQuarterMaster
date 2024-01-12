<?php

require __DIR__ . '/vendor/autoload.php';

use Ebprod\OqmCoreDepot\Context;
use Ebprod\OqmCoreDepot\EntryCategory;
use Ebprod\OqmCoreDepot\UiEntry;

//print "<h1>Hello, world!</h1>";
//phpinfo();

$numCorePlugins = 0;
$numInfraMetrics = 0;

$coreContent = "";
$pluginContent = "";
$metricsContent ="";
$infraContent = "";

$files = scandir(Context::$ENTRIES_DIR);
if($files){
	$entryArr = [];
	foreach ($files as $curFile) {
		if(!str_ends_with($curFile,".json")){
			continue;
		}
		$curEntry = new UiEntry(Context::$ENTRIES_DIR . $curFile);
		$entryArr[] = $curEntry;
	}
	
	foreach ($entryArr as $curEntry) {
		$curContent = '
<tr>
	<td><a href="'.$curEntry->getUrl().'" target="_self" title="Go to '.$curEntry->getName().'">'.$curEntry->getName().'</a></td>
	<td>'.$curEntry->getDescription().'</td>
	<td></td>
</tr>
';
		switch ($curEntry->getType()){
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

if($coreContent == ""){
	$coreContent = "<tr><td colspan='3'>None Available</td></tr>";
}
if($pluginContent == ""){
	$pluginContent = "<tr><td colspan='3'>None Available</td></tr>";
}
if($infraContent == ""){
	$infraContent = "<tr><td colspan='3'>None Available</td></tr>";
}
if($metricsContent == ""){
	$metricsContent = "<tr><td colspan='3'>None Available</td></tr>";
}

function getEntryTable($name, $content){
	return '
<h3 class="mt-5">'.$name.'</h3>
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
	'.$content.'
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
	<title>OQM Depot</title>
	<link href="/res/lib/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<main>
	<div class="container">
		<div class="mb-3 mt-3 text-center">
			<h1 class="display-5 fw-bold text-body-emphasis">OQM Depot</h1>
			<p class="lead mb-4">
				This is where you can access all your Open QuarterMaster components. Shown below are all available front-end interfaces you can visit and interact with.
			</p>
		</div>
		<ul class="nav nav-tabs" id="myTab" role="tablist">
			<li class="nav-item" role="presentation">
				<button class="nav-link active" id="home-tab" data-bs-toggle="tab" data-bs-target="#home-tab-pane" type="button" role="tab" aria-controls="home-tab-pane" aria-selected="true">
					Core & Plugins <span class="badge rounded-pill text-bg-dark"><?php echo $numCorePlugins; ?></span>
				</button>
			</li>
			<li class="nav-item" role="presentation">
				<button class="nav-link" id="profile-tab" data-bs-toggle="tab" data-bs-target="#profile-tab-pane" type="button" role="tab" aria-controls="profile-tab-pane" aria-selected="false">
					Metrics & Infrastructure <span class="badge rounded-pill text-bg-dark"><?php echo $numInfraMetrics; ?></span>
				</button>
			</li>
		</ul>
		<div class="tab-content" id="myTabContent">
			<div class="tab-pane fade show active" id="home-tab-pane" role="tabpanel" aria-labelledby="home-tab" tabindex="0">
				<h2 class="mt-2">
					Core components & Plugins
				</h2>
				<p>
					These components form the interfaces in which you interact with the system.
				</p>
				<?php echo getEntryTable("Core Components", $coreContent); ?>
				<?php echo getEntryTable("Plugins", $pluginContent); ?>
			</div>
			<div class="tab-pane fade" id="profile-tab-pane" role="tabpanel" aria-labelledby="profile-tab" tabindex="0">
				<h2 class="mt-2">
					Metrics & Infrastructure
				</h2>
				<p>
					Metrics keep track of how well things are running. Infrastructure components are on the backend helping the system run.
				</p>
				<?php echo getEntryTable("Metrics", $metricsContent); ?>
				<?php echo getEntryTable("Infra", $infraContent); ?>
			</div>
		</div>
	</div>
</main>

<script src="/res/lib/jquery/3.7.1/jquery.min.js.js"></script>
<script src="/res/lib/bootstrap/5.3.2/js/bootstrap.bundle.js"></script>
</body>
</html>
