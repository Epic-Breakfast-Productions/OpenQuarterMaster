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
	}
};


