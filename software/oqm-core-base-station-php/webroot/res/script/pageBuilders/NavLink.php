<?php
namespace Ebprod\OqmCoreDepot\pageBuilders;

/**
 * The nav links. Value is the served page
 */
enum NavLink: string {
	case overview = "/overview";
	case storage = "/storage";
	case items = "/items";
	case itemCategories = "/itemCategories";
	case itemCheckouts = "/itemCheckouts";
}
