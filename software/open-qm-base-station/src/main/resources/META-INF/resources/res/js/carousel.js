function clearCarousel(carousel) {
	carousel.find(".carousel-indicators").html("");
	carousel.find(".carousel-inner").html("");
}

/**
 * Data format:
 *   [
 *      {
 *          "src": "",
 *          "alt": "",
 *          "caption": "", (optional)
 *          "captionHeading": "", (optional)
 *      },
 *   ]
 */
function setCarouselImages(carousel, imageSrcs) {
	clearCarousel(carousel);
	var carouselIndicators = carousel.find(".carousel-indicators");
	var carouselImages = carousel.find(".carousel-inner");

	//clear carousel
	clearCarousel(carousel);

	//add images in
	imageSrcs.forEach(function (image, i) {
		var slideButton = $('<button type="button" data-bs-target="#' + carousel.attr('id') + '" data-bs-slide-to="' + i + '" ' + (i == 0 ? 'class="active" aria-current="true"' : '') + 'aria-label="Slide ' + (i + 1) + '"></button>');
		var imageDiv = $('<div class="carousel-item ' + (i == 0 ? 'active' : '') + '"> \
    <img src="' + image.src + '" class="d-block w-100" alt="' + image.alt + '"> \
</div>');

		carouselIndicators.append(slideButton);
		carouselImages.append(imageDiv);
	});
	carousel.carousel();
}

async function setCarouselImagesFromIds(imageIds, carousel) {
	// console.log("Getting item data for storage block \"" + blockId + "\"");
	return new Promise(async (done, fail) => {
		var ajaxPromises = []
		var carouselData = [];

		imageIds.forEach(function (id, i) {
			ajaxPromises.push(
				doRestCall({ //TODO:: move to getter
					spinnerContainer: carousel[0],
					url: "/api/v1/media/image/" + id,
					async: false,
					done: function (data) {
						carouselData[i] = {
							src: "/api/v1/media/image/" + id + "/data",
							alt: data.name,
							captionHeading: data.name,
							caption: data.description
						};
					}
				})
			);
		});

		for (let promise of ajaxPromises) {
			await promise;
		}

		setCarouselImages(carousel, carouselData);
	});
}