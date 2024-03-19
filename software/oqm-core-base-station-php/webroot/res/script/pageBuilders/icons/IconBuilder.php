<?php

namespace Ebprod\OqmCoreDepot\pageBuilders\icons;

class IconBuilder {
	
	public static function build(
		Icon   $icon,
		string $css = "",
		bool   $trailingSpace = false
	): string {
		// TODO:: secondary
		return '<i class="bi bi-' . $icon->getReference() . ' ' . $css . '" '.($icon->hasAltText()?'title="'.$icon->getAltText().'"':"").'>'.($trailingSpace?"&nbsp;":"").'</i>'.($trailingSpace?"&nbsp;":"");
	}
}