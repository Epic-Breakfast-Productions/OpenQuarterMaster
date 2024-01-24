<?php
require $_SERVER['CONTEXT_DOCUMENT_ROOT'] . '/vendor/autoload.php';
use Ebprod\OqmCoreDepot\pageBuilders\icons\Icon;
use Ebprod\OqmCoreDepot\pageBuilders\icons\IconBuilder;

header("Content-type: application/x-javascript");

$iconsToInclude = [
	"search" => Icon::$search
];
$icons = "";

foreach ($iconsToInclude as $iconName => $icon) {
	$icons .= "\t" . $iconName . ": '" . IconBuilder::build($icon) . "',\n";
}
?>
/**
 * Icons used throughout the site.
 * @type {{}}
 */
const Icons = {
<?php echo $icons; ?>
}
