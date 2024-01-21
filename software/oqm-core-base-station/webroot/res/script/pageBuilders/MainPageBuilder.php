<?php

namespace Ebprod\OqmCoreDepot\pageBuilders;

use Ebprod\OqmCoreDepot\Context;
use Ebprod\OqmCoreDepot\pageBuilders\icons\Icon;
use Ebprod\OqmCoreDepot\pageBuilders\icons\IconBuilder;

class MainPageBuilder {
	
	protected static function getNavEntry(NavEntry $navEntry):string{
		//TODO:: handle sub-groups
		return '
				<li class="nav-item">
					<a class="nav-link '.($navEntry->isActive()?"active":"").'" href="'.$navEntry->getPage()->getPath().'">
						'.IconBuilder::build($navEntry->getPage()->getIcon()).' '.$navEntry->getPage()->getTitle().'
						'.($navEntry->isActive()?'<span class="visually-hidden">(current)</span>':"").'
					</a>
				</li>
';
	}
	
	protected static function getNavItems():string{
		$output = '';
		
		$navEntries = NavEntry::getNavEntries();
		
		foreach ($navEntries as $curNavEntry){
			$output .= self::getNavEntry($curNavEntry);
		}
		
		return $output;
	}
	
	protected static function getNav():string{
		
		return '
<nav class="navbar navbar-expand-lg bg-light top-nav mb-2" data-bs-theme="light" id="top-nav">
	<div class="container-fluid">
		<a class="navbar-brand p-0" href="/overview.html">
			<img src="/res/media/logo.svg" alt="OQM Logo" id="topLogo">
		</a>
		<button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarColor03"
				aria-controls="navbarColor03" aria-expanded="false" aria-label="Toggle navigation">
			<span class="navbar-toggler-icon"></span>
		</button>
		<div class="collapse navbar-collapse" id="navbarColor03">
			<ul class="navbar-nav me-auto">
				'.self::getNavItems().'
			</ul>

			<form class="d-flex me-auto" method="get" action="/items" id="navSearchForm">
				<div class="input-group">
					<input class="form-control" id="navSearchInput" type="text" placeholder="Search" name="name">
					<select class="form-select" id="navSearchTypeSelect" aria-label="Navbar Quick Search search type" style="max-width: 140px;">
						<option data-action="/items" data-field="name" selected>Items</option>
						<option data-action="/storage" data-field="label">Storage Blocks</option>
					</select>
					<button class="btn btn-outline-dark" type="submit">'.IconBuilder::build(Icon::$search).' Search</button>
				</div>
			</form>
			<ul class="navbar-nav mb-auto">
				'.(
					Context::instance()->hasDepotUrl() ?
						'
						<li class="nav-item">
							<a class="nav-link" href="'.Context::instance()->getDepotUrl().'">
								Back to Depot
							</a>
						</li>
' : ''
			).'
				<li class="nav-item dropdown">
					<a class="nav-link dropdown-toggle" data-bs-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false">
						{#icons/user}{/icons/user}
							<span id="userNameDisplay">{userInfo.getName()}</span></a>
					<div class="dropdown-menu">
						<a class="dropdown-item" href="/you" id="youLink">
							{#icons/you}{/icons/you} You
								</a>
						<a class="dropdown-item" href="{config:[\'service.auth.userSettingsUrl\']}" id="youEditLink" target="_blank">
							{#icons/edit}{/icons/edit} Account Settings
								</a>
					{#if userInfo.getRoles().contains(\'userAdmin\') || userInfo.getRoles().contains(\'inventoryAdmin\')}
						<div class="dropdown-divider"></div>
					{/if}
					{#if userInfo.getRoles().contains(\'userAdmin\')}
						<a class="dropdown-item" href="/userAdmin" id="userAdminLink">
						{#icons/userAdmin}{/icons/userAdmin} User Administration
							</a>
					{/if}
					{#if userInfo.getRoles().contains(\'inventoryAdmin\')}
						<a class="dropdown-item" href="/inventoryAdmin" id="inventoryAdminLink">
						{#icons/inventoryAdmin}{/icons/inventoryAdmin} Inventory Admin
							</a>
					{/if}
					<div class="dropdown-divider"></div>
						<a class="dropdown-item" href="{config:[\'quarkus.oidc.logout.path\']}" id="logoutButton">
							{#icons/icon icon=\'door-closed\'}{/icons/icon} Logout
						</a>
					</div>
				</li>
			</ul>
		</div>
	</div>
</nav>

';
	}

	public static function getPageStart(
		string $pageStyle = ""
	):string {
		$page = Page::getPage();
		$styleSheets = "";
		return '
<!doctype html>
<html lang="en">
<head>
	<!-- Required meta tags -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

	<link rel="shortcut icon" href="/favicon.ico"/>
	<title>'.$page->getTitle().' - OQM Base Station</title>

	<!-- CSS -->
	<link href="/res/lib/bootstrap/5.3.2/yeti-bootswatch.min.css" rel="stylesheet">
	<link rel="stylesheet" href="/res/lib/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css">
	<link rel="stylesheet" href="/res/lib/spin.js/spin.css">
	<link rel="stylesheet" href="/res/lib/dselect/1.0.4/dist/css/dselect.min.css">
	<link rel="stylesheet" href="/res/css/bootstrap-adjust.css">
	<link rel="stylesheet" href="/res/css/main.css">
	'.$styleSheets.'
	<style>
		'.$pageStyle.'
	</style>
	<script src="/res/js/theme.js"></script>
</head>
<body class="min-vh-100 vstack">
<span id="pageInfo" data-page-initted="false"></span>
'.self::getNav().'
<div id="mainContainer" class="container flex-grow-1">
	<h1>
		'.IconBuilder::build($page->getIcon(), trailingSpace: true).$page->getTitle().'
	</h1>
	<hr />
	<div id="messageDiv">
	</div>

	<main class="" role="main" id="mainContent">
';
	}
	
	public static function getPageEnd(
		array $modals = [],
		string $pageScript = "",
		array $pageScriptFiles = []
	):string {
		$scriptFiles = "";
		foreach ($pageScriptFiles as $pageScriptFile) {
			$scriptFiles .= '<script src="'.$pageScriptFile.'"></script>\n';
		}
		
		return '
	</main>
</div>
<footer id="footer" class="container mb-3" role="contentinfo">
	<hr/>
	<div class="row">
		<div class="col-sm-4">
			<span class="h5">Open QuarterMaster Base Station</span><br/>
			Version <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster" target="_blank">'.Context::instance()->getBsVersion().'</a>, &copy; '.date("Y").' <a href="https://epic-breakfast-productions.tech/" target="_blank">EBP <img src="/res/media/EBP-logo-icon.svg" style="max-height:1.2em;" alt="EBP Logo"/></a><br/>
			Released under the <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/LICENSE" target="_blank">GPL v3.0 License</a><br/>
			<div class="dropup color-modes" id="theme-picker">
				<button class="btn btn-link p-0 text-decoration-none dropdown-toggle"
						id="bd-theme"
						type="button"
						aria-expanded="false"
						data-bs-toggle="dropdown"
						data-bs-display="static">
					<span class="theme-icon-active">
						'.IconBuilder::build(Icon::$themeAuto).'
							</span>
							<span id="bd-theme-text">
							Toggle theme
						</span>
				</button>
				<ul class="dropdown-menu" aria-labelledby="bd-theme" style="--bs-dropdown-min-width: 8rem;">
					<li>
						<button type="button" class="dropdown-item d-flex align-items-center" data-bs-theme-value="light">
							<span class="theme-icon">
								'.IconBuilder::build(Icon::$themeLight).'
									</span>
								Light
						</button>
					</li>
					<li>
						<button type="button" class="dropdown-item d-flex align-items-center" data-bs-theme-value="dark">
							<span class="theme-icon">
								'.IconBuilder::build(Icon::$themeDark).'
									</span>
								Dark
						</button>
					</li>
					<li>
						<button type="button" class="dropdown-item d-flex align-items-center active" data-bs-theme-value="auto">
							<span class="theme-icon">
								'.IconBuilder::build(Icon::$themeAuto).'
									</span>
								Auto
						</button>
					</li>
					<li style=" height: 5px;">
						<button type="button" class="dropdown-item d-flex align-items-center" style="font-size: 0.15em; height: 5px;" onclick="if(typeof RealDarkMode !== \'undefined\'){ RealDarkMode.realDarkMode();}else{ script=document.createElement(\'script\');script.src = \'/res/js/realDarkMode.js\';document.head.appendChild(script);}">
							<span class="theme-icon">
								'.IconBuilder::build(Icon::$themeDark).'
									</span>
								Really Dark
								</button>
					</li>
				</ul>
			</div>
			<a href="/help">'.IconBuilder::build(Icon::$help).' Help & User Guide</a><br />
			<small class="fw-lighter fst-italic text-muted">
				<div class="d-grid gap-2">
					<button class="btn btn-outline-success btn-sm" type="button" data-bs-toggle="collapse"
							data-bs-target="#pageLoadInfoCollapse" aria-expanded="false"
							aria-controls="pageLoadInfoCollapse">
								Page Loaded: <span id="pageLoadTimestamp">{generateDatetime.format(dateTimeFormatter)}</span>
									(Server time)
					</button>
				</div>
				<div class="collapse" id="pageLoadInfoCollapse">
					<div class="card card-body">
									Service id: <code class="user-select-all"
								id="traceServiceName">{config:[\'quarkus.application.name\']}</code><br/>
						Trace id: <code class="user-select-all" id="traceId">{traceId}</code>
						<!-- TODO:: link to Jaeger? -->
					</div>
				</div>
			</small>
		</div>
		<div id="serverInfo" class="col-sm-4">
			{#if config:[\'service.runBy.logo\'] != " "}
				<img src="/api/v1/media/runBy/logo" style="float:right; max-width: 30%;">
			{/if}
			{#if config:[\'service.runBy.name\'] != " "}
				<span class="h5">Run by:</span><br/>
				{config:[\'service.runBy.name\']}
			{/if}
			<br/>
			{#if config:[\'service.runBy.email\'] != " " || config:[\'service.runBy.phone\'] != " " || config:[\'service.runBy.website\'] != " "}
				<span class="h6">Contact Info:</span><br/>
				{#if config:[\'service.runBy.email\'] != " "}<a href="mailto:{config:[\'service.runBy.email\']}">{config:[\'service.runBy.email\']}</a>
					<br/>{/if}
				{#if config:[\'service.runBy.phone\'] != " "}<a href="tel:{config:[\'service.runBy.phone\']}">{config:[\'service.runBy.phone\']}</a>
					<br/>{/if}
				{#if config:[\'service.runBy.website\'] != " "}<a href="{config:[\'service.runBy.website\']}">{config:[\'service.runBy.website\']}</a>
					<br/>{/if}
			{/if}
		</div>
		<div class="col-sm-4">
			{config:[\'service.runBy.motd\']}
		</div>
	</div>
</footer>

<!-- Modals -->
'.implode("\n", $modals).'

<!-- scripts -->
<script src="/res/lib/jquery/3.7.1/jquery.min.js"></script>
<script src="/res/lib/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
<script src="/res/lib/luxon/3.3.0/luxon.min.js"></script>
<script src="/res/lib/js-cookie-3.0.1/js.cookie.min.js"></script>
<script src="/res/lib/spin.js/spin.umd.js"></script>
<script src="/res/lib/dselect/1.0.4/dist/js/dselect.js"></script>
<script src="/res/js/otherUtils/spinnerHelpers.js"></script>
<script src="/res/js/page/getParamUtils.js"></script>
<script src="/res/js/page/pageMessages.js"></script>
<script src="/res/js/inputs/dselectHelpers.js"></script>
<script src="/res/js/rest.js"></script>
<script src="/res/js/otherUtils/timeHelpers.js"></script>
<script src="/res/js/icons.js.php"></script>
<script src="/res/js/main.js"></script>
<!-- Extra Page Script files -->
'.$scriptFiles.'
<!-- End Page Script files -->
<!-- Extra Page Script -->
'.$pageScript.'
<!-- End Extra Page Script -->
</body>
</html>
';
	}
	
	public static function getWholePage(
		string $content
	):string {
		return self::getPageStart() . '
' . $content . '
' . self::getPageEnd();
	}
}