
/**
 * Class to be inherited by all page utilities. Outlines basic specs for being a page utility.
 */
export class PageUtility {

	static {
		//
	}

	/**
	 * Call this method to initialize the class. Moreseo to ensure proper setup and initialization of the inheriting class.
	 */
	static init(){
		console.debug(this.name + " initialization assured.");
	}
}