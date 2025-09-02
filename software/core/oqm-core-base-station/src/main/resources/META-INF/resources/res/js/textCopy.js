
const TextCopyUtils = {
	copyText(buttonClicked, textContainerJq) {
		let copyText = textContainerJq.text();

		console.debug("Writing text to clipboard: ", copyText);

		navigator.clipboard.writeText(copyText);

		buttonClicked = $(buttonClicked);
		buttonClicked.html(Icons.copyChecked);
		setTimeout(
			function () {
				// console.log("Setting copy symbol back.");
				buttonClicked.html(Icons.copy);
			},
			5_000
		);
	}
}
