<?php

require __DIR__ . '/vendor/autoload.php';

use Ebprod\OqmCoreDepot\Context;
use Ebprod\OqmCoreDepot\EntryCategory;
use Ebprod\OqmCoreDepot\UiEntry;

?>
<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="shortcut icon" href="/favicon.ico"/>
	<title>OQM Depot</title>
	<link href="/res/lib/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="min-vh-100 vstack">
<main class="container flex-grow-1">
	<div class="mb-3 mt-3 text-center">
		<img src="/res/media/logo.svg" class="" style="max-width: 75%;" alt="OQM Logo">
		<h1 class="display-5 fw-bold text-body-emphasis">OQM Base Station</h1>
		<p class="lead mb-4">
			This is where you can access all your Open QuarterMaster components. Shown below are all available front-end interfaces you can visit and interact with.
		</p>
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
				<img src="/res/media/EBP-logo-icon.svg" style="max-height:1.2em;" alt="EBP Logo"/></a><br/>
			Released under the
			<a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/LICENSE" target="_blank">GPL v3.0 License</a><br/>
		</div>
	</div>
</footer>

<script src="/res/lib/jquery/3.7.1/jquery.min.js.js"></script>
<script src="/res/lib/bootstrap/5.3.2/js/bootstrap.bundle.js"></script>
</body>
</html>
