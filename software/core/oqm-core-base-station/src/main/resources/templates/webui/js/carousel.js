//TODO:: determine if necessary
const Carousel = {
	carouselTemplate: '{carouselLines}\
	',
	newCarousel(id, objectData=null, toAppendTo=null){
		return new Promise(async (done, fail) => {
			let newCarousel = $(Carousel.carouselTemplate);
			newCarousel.prop("id", id);

			//prev/next setup
			newCarousel.find(".carousel-control-prev-next").attr("data-bs-target", "#"+id);

			//populate
			let promises = [];

			if(objectData) {
				promises.push(Carousel.processImagedObjectImages(objectData, newCarousel));
			}
			if(toAppendTo){
				toAppendTo.append(newCarousel);
			}

			await Promise.all(promises);

			return newCarousel;
		});
	},

	clearCarousel(carousel) {
		carousel.find(".carousel-indicators").html("");
		carousel.find(".carousel-inner").html("");
	},
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
	setCarouselImages(carousel, imageSrcs) {
		Carousel.clearCarousel(carousel);
		var carouselIndicators = carousel.find(".carousel-indicators");
		var carouselImages = carousel.find(".carousel-inner");

		//clear carousel
		Carousel.clearCarousel(carousel);

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
	},
	async setCarouselImagesFromIds(imageIds, carousel) {
		console.log("Adding images to carousel: ", imageIds);
		return new Promise(async (done, fail) => {
			var ajaxPromises = []
			var carouselData = [];

			imageIds.forEach(function (id, i) {
				console.debug("Adding image to carousel: ", id);
				ajaxPromises.push(
					Rest.call({ //TODO:: move to getter
						spinnerContainer: carousel[0],
						url: Rest.passRoot + "/media/image/" + id,
						async: false,
						done: function (data) {
							carouselData[i] = {
								src: Rest.passRoot + "/media/image/" + id + "/revision/latest/data",
								alt: data.name,
								captionHeading: data.name,
								caption: data.description
							};
						}
					})
				);
			});

			await Promise.all(ajaxPromises);

			Carousel.setCarouselImages(carousel, carouselData);
		});
	},
	async processImagedObjectImages(objectData, carousel) {
		if (objectData.imageIds.length) {
			console.log("Object had images to show.");
			carousel.show();
			return Carousel.setCarouselImagesFromIds(objectData.imageIds, carousel);
		}
		console.log("Object had no images to show.");
		carousel.hide();
		return [];
	}
}




