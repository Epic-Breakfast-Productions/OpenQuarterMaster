//TODO:: helpers for datetime inputs; setting value, getting value, dealing with timezones
const TimeHelpers = {

	/**
	 * Get the current timestamp used for `datetime-local` inputs.
	 */
	getNowTs(){
		return luxon.DateTime.now().toISO().slice(0, 16);
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


