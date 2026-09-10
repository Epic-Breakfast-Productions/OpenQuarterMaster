const navSearchInput = $('#navSearchInput');
const navSearchForm = $('#navSearchForm');
const navSearchTypeSelect = $('#navSearchTypeSelect');

function updateNavSearchDestination(action, icon, fieldName) {
	navSearchForm.attr("action", action);
	navSearchTypeSelect.html(icon);
	navSearchInput.attr("name", fieldName);
}

const Main = {
	/**
	 *
	 *  @param process {string|null} The name of the process we are starting. Used for logging.
	 *  @param spinner {boolean|HTMLElement}
	 */
	processCount: 0,
	processStart(process = null, spinner = false) {
		this.processCount++;
		if(spinner !== false){
			SpinnerUtils.startSpinner(
				spinner === true ? null : spinner
			);
		}
		if (process) {
			console.log("Started process " + process);
		}
	},
	processStop(process = null, spinner=false) {
		if(Main.processCount <= 0){
			console.error("Tried to stop a process that was never started! (Process count before stop: " + Main.processCount +")");
			return;
		}

		console.debug("Stopping process.")

		Main.processCount--;
		if(spinner){
			SpinnerUtils.stopSpinner();
		}
		if (process) {
			console.log("Finished process " + process);
		}
		if(Main.processCount == 0){
			SpinnerUtils.resetAllSpinner();
			console.log("All processes finished.");
		}
	},
	processesRunning() {
		return Main.processCount !== 0;
	},
	noProcessesRunning() {
		return !Main.processesRunning();
	},
	/**
	 * @async
	 * @function waitUntilTrue
	 * @param {() => boolean} conditionFunction
	 * @param {number} [interval=10]
	 * @param {number} [timeout=10000]
	 * @param {boolean} [throwOnTimeout=false]
	 * @returns {Promise<void>}
	 */
	waitUntilTrue: async function (
		conditionFunction,
		interval = 10,
		timeout = 10_000,
		throwOnTimeout = false,
	) {
		if (conditionFunction()) {
			return;
		}

		let timePassed = 0;
		return new Promise(function poll(resolve, reject) {
			if (timePassed >= timeout) {
				return throwOnTimeout ? reject() : resolve();
			}
			if (conditionFunction()) {
				return resolve();
			}
			timePassed += interval;
			setTimeout(() => poll(resolve, reject), interval);
		});
	},

	pageModulesLoaded(){
		Main.processStop("Main page load", true);
	}
}

Main.processStart("Main page load", true);


const UserUtils = {
	userId: $("#userNameDisplay").data("userid")
}

console.log("===== New Page Loading =====");
let popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
let popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
	return new bootstrap.Popover(popoverTriggerEl)
});
Main.processStart("Main page modules load");
