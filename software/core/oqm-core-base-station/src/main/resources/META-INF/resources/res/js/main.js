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
	 * Initial value for processes:
	 *  - page messages
	 *  - time helpers
	 *  - this popovers
	 *  - dselect
	 */
	processCount: 4,
	processStart(process = null) {
		this.processCount++;
		if (process) {
			console.log("Started process " + process);
		}
	},
	processStop(process = null) {
		if(Main.processCount <= 0){
			log.error("Tried to stop a process that was never started! (Process count before stop: " + Main.processCount +")");
		}

		Main.processCount--;
		if (process) {
			console.log("Finished process " + process);
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
	}
}

const UserUtils = {
	userId: $("#userNameDisplay").data("userid")
}

console.log("===== New Page Loading =====");
let popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
let popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
	return new bootstrap.Popover(popoverTriggerEl)
});
Main.processStop();