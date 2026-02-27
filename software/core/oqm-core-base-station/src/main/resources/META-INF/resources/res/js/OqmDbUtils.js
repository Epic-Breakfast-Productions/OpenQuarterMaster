export const OqmDbUtils = {
	navDbSelectForm: $("#navDbSelectForm"),
	navDatabaseSelector: $("#navDatabaseSelector"),
	newDbSelected: function () {
		let newDbId = this.navDatabaseSelector.val();
		console.log("Input to select database changed. New selection: ", newDbId);

		if (confirm("Are you sure you want to swap databases?")) {
			console.log("User confirmed database switch.");

			Cookies.set('oqmDb', newDbId, {expires: 365, sameSite: 'strict'});
			PageMessageUtils.reloadPageWithMessage("Successfully swapped databases.", "success");
		} else {
			console.log("User canceled db swap.");
			this.navDbSelectForm.trigger('reset');
		}
	}
}