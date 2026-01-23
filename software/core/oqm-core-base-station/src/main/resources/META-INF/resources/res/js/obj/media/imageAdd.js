var defaultCroppieImage = Rest.webroot + "/media/logoSymbolSquare.svg";

$uploadCrop = $('#imageUploadCroppieDiv').croppie({
	enableExif: true,
	viewport: {
		width: 250,
		height: 250,
		type: 'square'
	},
	boundary: {
		width: 300,
		height: 300
	}
});
console.log("Created croppie instance: ", $uploadCrop);

var cropSlider = $uploadCrop.find(":input.cr-slider");

function bindCroppie(bindVal){
	console.log("Binding croppie to image: ", bindVal);
	return $uploadCrop.croppie('bind', {
		url: bindVal
	}).then(function(){
		var minVal = cropSlider.attr('min');
		minVal = minVal === undefined ? 0 : minVal;
		$uploadCrop.croppie(
			'setZoom',
			cropSlider.attr('min')
		);
	});
}

$('#imageUploadInput').on('change', function () {
	console.log("Got new image to put in croppie.");
	var reader = new FileReader();
	reader.onload = function (e) {
		bindCroppie(e.target.result).then(function(){
			console.log('Loaded image selected by user.');
		});
	}
	reader.readAsDataURL(this.files[0]);
});


function resetCroppie(){
	console.log("Resetting croppie.");
	bindCroppie(defaultCroppieImage);
}