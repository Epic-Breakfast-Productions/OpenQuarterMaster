
/**
 * Class to be inherited by all page utilities. Outlines basic specs for being a page utility.
 */
export class PageUtility {

	static {
		//nothing to do
	}

	/**
	 * Call this method to initialize the class. Moreseo to ensure proper setup and initialization of the inheriting class, appease "unused" code import warnings
	 */
	static init(){
		console.debug(this.name + " initialization assured.");
	}
}