
const ModalHelpers = {

	/**
	 *
	 * @param destModalJq The jQuery object of the modal we are going to.
	 * @param returnModal Mixed type. If String, the tag to find using jquery. Can also be the modal element or an element within the modal both as plain js or jQuery
	 */
	setReturnModal: function(destModalJq, returnModal){
		//ensure using jquery object
		if(returnModal == null){
			return; //TODO:: reset return modal
		} else if(typeof returnModal === "string" || returnModal instanceof String || returnModal instanceof Element){
			returnModal = $(returnModal);
		} else if(returnModal instanceof Event){
			returnModal = $(returnModal.target);
		}
		//get modal parent, if applicable
		returnModal = returnModal.closest(".modal");

		//TODO:: set destModal to return to returnModal.




	}


};