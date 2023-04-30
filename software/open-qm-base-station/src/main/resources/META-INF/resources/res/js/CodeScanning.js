/**
 *
 * Requires:
 *
 *  -
 *
 * @type {{scanningModal: (*|jQuery|HTMLElement), codeScanner}}
 */
const CodeScanner = {
	scanningModal: $("#codeScannerModal"),
	scanningModalObj: new bootstrap.Modal($("#codeScannerModal")),
	codeScanner: new Html5QrcodeScanner(
		"codeScannerRender",
		{fps: 15, qrbox: {width: 250, height: 250}},
		/* verbose= */ false
	),
	codeDestInput: null,
	otherModal: null,
	cleanupScanner(event){
		CodeScanner.codeScanner.clear();
		CodeScanner.codeDestInput = null;

		if(CodeScanner.otherModal != null) {
			new bootstrap.Modal($("#"+CodeScanner.otherModal)).show();
			CodeScanner.otherModal = null;
		}
	},
	onScanSuccess(decodedText, decodedResult) {
		// handle the scanned code as you like, for example:
		console.log(`Code matched = ${decodedText}`, decodedResult);

		if(CodeScanner.codeDestInput){
			CodeScanner.codeDestInput.val(decodedText);
		} else {
			console.warn("No destination input!");
		}
		CodeScanner.scanningModalObj.hide();
	},
	onScanFailure(error) {
		console.warn(`Code scan error = ${error}`);
	}
};

CodeScanner.scanningModal.on('hidden.bs.modal', CodeScanner.cleanupScanner);
CodeScanner.scanningModal.on('show.bs.modal', event => {
	console.log("Opening Barcode Scanning Modal");
	CodeScanner.codeScanner.render(CodeScanner.onScanSuccess, CodeScanner.onScanFailure);
	CodeScanner.codeDestInput = $(event.relatedTarget.nextElementSibling);

	let inputModal = CodeScanner.codeDestInput.closest(".modal");
	if(inputModal.length){
		CodeScanner.otherModal = inputModal[0].id;
	}

});
