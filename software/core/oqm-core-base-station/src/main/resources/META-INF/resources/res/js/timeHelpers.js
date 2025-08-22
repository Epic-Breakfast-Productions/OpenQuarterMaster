/**
 *
 * TODO:: function to turn dt from server to client's tz, format
 *
 * https://moment.github.io/luxon/demo/global.html
 * @type {{getNowTs(): *, setupDateTimeInputs(): void, getTsForServer(*): (null|*)}}
 */
const TimeHelpers = {

	/**
	 * Get the current timestamp used for `datetime-local` inputs.
	 */
	getNowTs(){
		return luxon.DateTime.now().toISO().slice(0, 16);
	},
	getTsFromInput(dtInputJq){
		let value = dtInputJq.val();

		if(!value){
			console.log("No dt value.");
			return null;
		}

		value = luxon.DateTime.fromISO(value);
		value = value.toISO();

		console.log("Returning dt value: " + value);
		return value;
	},
	setupDateTimeInputs(){
		let nowDateTimeStamp = this.getNowTs();
		let futureInputs = $(".datetimeInputFuture");

		if(futureInputs.length > 0 ) {
			console.log("Setting future inputs with min time " + nowDateTimeStamp);
			futureInputs.each(function (i, element) {
					element.min = nowDateTimeStamp;
				});
		}
	},
	durationNumSecsToTimespan(duration){
		if ((duration / 604800) % 1 == 0) {
			console.log("Determined was weeks.");
			return "weeks";
		} else if ((duration / 86400) % 1 == 0) {
			console.log("Determined was days.");
			return "days";
		} else if ((duration / 3600) % 1 == 0) {
			console.log("Determined was hours.");
			return "hours";
		} else if ((duration / 60) % 1 == 0) {
			console.log("Determined was minutes.");
			return "minutes";
		} else {
			console.log("Determined was seconds.");
			return "seconds";
		}
	},
	durationNumSecsTo(duration, timespanTo = null){
		if(timespanTo == null){
			timespanTo = TimeHelpers.durationNumSecsToTimespan(duration);
		}
		console.debug("Turning duration " + duration + " to " + timespanTo)
		let output = null;
		switch (timespanTo) {
			case "weeks":
				output = duration / 604800;
				break;
			case "days":
				output = duration / 86400;
				break;
			case "hours":
				output = duration / 3600;
				break;
			case "minutes":
				output = duration / 60;
				break;
			default:
				output = duration;
				break;
		}
		console.debug("Turned duration " + duration + " to " + output + " " + timespanTo);
		return output;
	},
	durationNumSecsToHuman(duration){
		let timespan = TimeHelpers.durationNumSecsToTimespan(duration);
		let num = TimeHelpers.durationNumSecsTo(duration, timespan);
		return num + " " + timespan;
	}
};


TimeHelpers.setupDateTimeInputs();
Main.processStop();

