const FileAttachmentView = {
	viewModal: $("#fileAttachmentViewModal"),
	viewModalMessages: $("#fileAttachmentViewMessages"),
	previewContainer: $("#fileAttachmentViewPreviewContainer"),
	fullViewContainer: $("#fileAttachmentViewFullContainer"),
	fileViewDownloadButton: $("#fileAttachmentViewDownloadButton"),
	contentViewDownloadButton: $("#fileAttachmentViewContentDownloadButton"),

	resetView() {
		this.previewContainer.text("");
		this.fullViewContainer.text("");
		this.fileViewDownloadButton.prop("href", "");
		this.contentViewDownloadButton.prop("href", "");
	},
	setupFileView(fileGetData, container, preview = true){
		let latestMetadata = fileGetData.revisions[0];
		let newContent;
		if(latestMetadata.mimeType.startsWith("audio/")){
			newContent = $(' <audio controls>\n' +
				'  <source src="/api/v1/media/fileAttachments/'+ fileGetData.id + '/data" type="'+latestMetadata.mimeType+'">\n' +
				'</audio> ');
			newContent.on("stalled", function (e){
				let code = newContent[0].error.code
				console.log("Finished loading audio. Error: ", e);
				console.log("Error code: ", code);
			});
		} else if(latestMetadata.mimeType.startsWith("video/")){
			newContent = $(' <video controls style="max-width: 100%;">\n' +
				'  <source src="/api/v1/media/fileAttachments/'+ fileGetData.id + '/data" type="'+latestMetadata.mimeType+'">\n' +
				'</video> ');
			newContent.on("stalled", function (e){
				let code = newContent[0].error.code
				console.log("Finished loading audio. Error: ", e);
				console.log("Error code: ", code);
			});
		} else if(latestMetadata.mimeType.startsWith("image/")){
			newContent = $(' <img src="/api/v1/media/fileAttachments/'+ fileGetData.id + '/data" style="max-width: 100%;" alt="">\n');
		} else if(!preview){//only show these if we are not previewing
			//TODO:: show pdf, text, markdown?
		}
		container.append(newContent);
	},

	setupView(fileId) {
		console.log("Setting up view for file ", fileId);
		this.resetView();
		this.fileViewDownloadButton.prop("href", '/api/v1/media/fileAttachments/'+ fileId + '/data');
		this.contentViewDownloadButton.prop("href", '/api/v1/media/fileAttachments/'+ fileId + '/data');

		doRestCall({
			spinnerContainer: FileAttachmentView.viewModal,
			url: "/api/v1/media/fileAttachments/" + fileId,
			failMessagesDiv: FileAttachmentView.viewModalMessages,
			done: async function (data) {
				console.log("Got file info: ", data);
				let latestMetadata = data.revisions[0];
				FileAttachmentView.setupFileView(data, FileAttachmentView.previewContainer);
				FileAttachmentView.setupFileView(data, FileAttachmentView.fullViewContainer, false);
			}
		});
	}
};