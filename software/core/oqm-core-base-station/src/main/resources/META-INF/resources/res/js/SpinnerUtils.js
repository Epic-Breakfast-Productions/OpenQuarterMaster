// import {Spinner} from "../../lib/spin.js/spin.js";
class SpinnerUtils {
	static #spinnerOpts = {
		lines: 20, // The number of lines to draw
		length: 38, // The length of each line
		width: 17, // The line thickness
		radius: 45, // The radius of the inner circle
		scale: 1, // Scales overall size of the spinner
		corners: 1, // Corner roundness (0..1)
		color: '#868686', // CSS color or array of colors
		fadeColor: 'transparent', // CSS color or array of colors
		speed: 1, // Rounds per second
		rotate: 0, // The rotation offset
		animation: 'spinner-line-shrink', // The CSS animation name for the lines
		direction: 1, // 1: clockwise, -1: counterclockwise
		zIndex: 2e9, // The z-index (defaults to 2000000000)
		className: 'spinner', // The CSS class to assign to the spinner
		top: '50%', // Top position relative to parent
		left: '50%', // Left position relative to parent
		shadow: '0 0 1px transparent', // Box-shadow for the lines
		position: 'absolute' // Element positioning
	}

	/**
	 *
	 * @type {Spinner|null}
	 */
	static #curSpinner = null;
	static #spinnerCount = 0;

	static hasSpinner(){
		return this.#curSpinner != null;
	}


	static stopSpinner(){
		this.#spinnerCount--;
		if(this.#spinnerCount < 1){
			this.resetAllSpinner();
		}
	}

	static resetAllSpinner(){
		if(this.hasSpinner()){
			this.#curSpinner.stop();
		}
		this.#curSpinner = null;
		this.#spinnerCount = 0;
	}

	/**
	 * Starts a spinner on a given html element.
	 * @param container {HTMLElement|null} Defaults to document body
	 * @param opts {{lines: number, length: number, width: number, radius: number, scale: number, corners: number, color: string, fadeColor: string, speed: number, rotate: number, animation: string, direction: number, zIndex: number, className: string, top: string, left: string, shadow: string, position: string}}
	 */
	static startSpinner(container = null, opts = this.#spinnerOpts){
		this.#spinnerCount++;

		if(this.hasSpinner()){
			console.debug("Incremented already running spinner", this.#spinnerCount);
			return;
		}

		console.log("Starting new spinner. Container:", container);

		let output = new Spin.Spinner(opts);

		if(container == null){
			container = document.body;
		}

		output.spin(container);

		this.#curSpinner = output;
	}
}
