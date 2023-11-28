const FileAttachmentView = {
	viewModal: $("#fileAttachmentViewModal"),
	viewModalMessages: $("#fileAttachmentViewMessages"),
	previewContainer: $("#fileAttachmentViewPreviewContainer"),
	fullViewTitle: $("#fileAttachmentViewContentModalLabel"),
	fullViewContainer: $("#fileAttachmentViewFullContainer"),
	fileViewDownloadButton: $("#fileAttachmentViewDownloadButton"),
	contentViewDownloadButton: $("#fileAttachmentViewContentDownloadButton"),
	fileName: $("#fileAttachmentViewFileName"),
	description: $("#fileAttachmentViewDescription"),
	keywords: $("#fileAttachmentViewKeywordsSection"),
	atts: $("#fileAttachmentViewAttsSection"),
	numRevisions: $("#fileAttachmentViewRevisionsExpandNumRevisions"),
	revisionsAccord: $("#fileAttachmentViewRevisionsAccord"),

	resetView() {
		this.previewContainer.text("");
		this.fullViewContainer.text("");

		this.fileName.text("");
		this.fullViewTitle.text("");
		this.fileViewDownloadButton.prop("href", "");
		this.contentViewDownloadButton.prop("href", "");
		this.description.text("");
		this.numRevisions.text("0");
		this.revisionsAccord.text("");
		clearHideKeywordDisplay(this.keywords);
		clearHideAttDisplay(this.atts);
	},
	setupFileView(fileGetData, container, preview = true) {
		let latestMetadata = fileGetData.revisions[0];
		let newContent;
		if (latestMetadata.mimeType.startsWith("audio/")) {
			newContent = $(' <audio controls>\n' +
				'  <source src="/api/v1/media/fileAttachments/' + fileGetData.id + '/data" type="' + latestMetadata.mimeType + '">\n' +
				'</audio> ');
			newContent.on("stalled", function (e) {
				let code = newContent[0].error.code
				console.log("Finished loading audio. Error: ", e);
				console.log("Error code: ", code);
			});
		} else if (latestMetadata.mimeType.startsWith("video/")) {
			newContent = $(' <video controls style="max-width: 100%;">\n' +
				'  <source src="/api/v1/media/fileAttachments/' + fileGetData.id + '/data" type="' + latestMetadata.mimeType + '">\n' +
				'</video> ');
			newContent.on("stalled", function (e) {
				let code = newContent[0].error.code
				console.log("Finished loading audio. Error: ", e);
				console.log("Error code: ", code);
			});
		} else if (latestMetadata.mimeType.startsWith("image/")) {
			newContent = $(' <img src="/api/v1/media/fileAttachments/' + fileGetData.id + '/data" style="max-width: 100%;" alt="">\n');
		} else if (!preview) {//only show these if we are not previewing
			if (latestMetadata.mimeType === "application/pdf") {
				//TODO:: neither of these work
				newContent = $('<object style="width: 100%; height: 500px;" type="application/pdf" data="/api/v1/media/fileAttachments/' + fileGetData.id + '/data"><p>Failed to load pdf.</p></object>');
				// newContent = $('<embed src="/api/v1/media/fileAttachments/'+ fileGetData.id + '/data" width="500" height="375" />');
			}
			//TODO:: show pdf, text, markdown?
		}
		container.append(newContent);
	},

	createFileHashView(hashes){
		let output = $("<div></div>");

		Object.entries(hashes).forEach(entry => {
			const [hash, value] = entry;
			output.append(
				$('<p></p>')
					.append($('<span></span>').text(hash + ": "))
					.append($('<code class="user-select-all"></code>').text(value))
			);
		});

		return output;
	},
	createFileMetadataView(fileMetadata) {
		let output = $('<div></div>');

		output.append(
			$('<div class="row"></div>')
				.append(
					$('<div class="col"></div>')
						.append($('<h5>Upload Datetime:</h5>'))
						.append($('<p class="user-select-all"></p>').text(fileMetadata.uploadDateTime))
				));

		output.append(
			$('<div class="row"></div>')
				.append(
					$('<div class="col"></div>')
						.append($('<h5>Filename:</h5>'))
						.append($('<p class="user-select-all"></p>').text(fileMetadata.origName))
				));
		output.append(
			$('<div class="row"></div>')
				.append(
					$('<div class="col"></div>')
						.append($('<h5>Length:</h5>'))
						.append($('<p class="user-select-all"></p>').text(OtherUtils.numBytesToHuman(fileMetadata.length)))
				));
		output.append(
			$('<div class="row"></div>')
				.append(
					$('<div class="col"></div>')
						.append($('<h5>MimeType:</h5>'))
						.append($('<p class="user-select-all"></p>').text(fileMetadata.mimeType))
				));
		output.append(
			$('<div class="row"></div>')
				.append(
					$('<div class="col"></div>')
						.append($('<h5>Hashes:</h5>'))
						.append(FileAttachmentView.createFileHashView(fileMetadata.hashes))
				));

		return output;
	},

	setupView(fileId) {
		console.log("Setting up view for file ", fileId);
		this.resetView();
		this.fileViewDownloadButton.prop("href", '/api/v1/media/fileAttachments/' + fileId + '/data');
		this.contentViewDownloadButton.prop("href", '/api/v1/media/fileAttachments/' + fileId + '/data');

		doRestCall({
			spinnerContainer: FileAttachmentView.viewModal,
			url: "/api/v1/media/fileAttachments/" + fileId,
			failMessagesDiv: FileAttachmentView.viewModalMessages,
			done: async function (data) {
				console.log("Got file info: ", data);
				let latestMetadata = data.revisions[0];
				FileAttachmentView.fileName.text(latestMetadata.origName);
				FileAttachmentView.fullViewTitle.text(latestMetadata.origName);
				FileAttachmentView.setupFileView(data, FileAttachmentView.previewContainer);
				FileAttachmentView.setupFileView(data, FileAttachmentView.fullViewContainer, false);
				processKeywordDisplay(FileAttachmentView.keywords, data.keywords);
				processAttDisplay(FileAttachmentView.atts, data.attributes);
				FileAttachmentView.description.text(data.description);
				FileAttachmentView.numRevisions.text(data.revisions.length);

				data.revisions.forEach(function (curMetadata, i) {
					let newAccordItem = $('<div class="accordion-item">\n' +
						'    <h2 class="accordion-header">\n' +
						'        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#fileAttachmentViewRevisionsAccord-' + i + '" aria-expanded="false" aria-controls="fileAttachmentViewRevisionsAccord-' + i + '">\n' +
						'            ' + i + (i === 0 ? " (Latest)" : "") + '\n' +
						'        </button>\n' +
						'    </h2>\n' +
						'    <div id="fileAttachmentViewRevisionsAccord-' + i + '" class="accordion-collapse collapse" data-bs-parent="#fileAttachmentViewRevisionsAccord">\n' +
						'        <div class="accordion-body" id="fileAttachmentViewRevisionsAccordBody-' + i + '">\n' +
						'        </div>\n' +
						'    </div>\n' +
						'</div>');
					newAccordItem.find("#fileAttachmentViewRevisionsAccordBody-" + i).append(FileAttachmentView.createFileMetadataView(curMetadata));
					FileAttachmentView.revisionsAccord.append(newAccordItem);
				})
			}
		});
	},
	resetObjectView(fileObjViewContainerJq){
		fileObjViewContainerJq.find(".fileAttachmentListTableContent").text("");
		fileObjViewContainerJq.hide();
	},
	setupObjectView(fileObjViewContainerJq, fileList, failMessagesDiv){
		this.resetObjectView(fileObjViewContainerJq);
		if(fileList.length === 0){
			console.log("No files to show.");
			return;
		}

		let fileShowContent = fileObjViewContainerJq.find(".fileAttachmentListTableContent");

		fileList.forEach(function (curFileId, i){
			doRestCall({
				spinnerContainer: null,
				url: "/api/v1/media/fileAttachments/" + curFileId,
				failMessagesDiv: failMessagesDiv,
				done: async function (data) {
					let nextRow = $('<tr></tr>');
					nextRow.append($('<td></td>').text(data.revisions[0].origName));
					nextRow.append($('<td><button type="button" class="btn btn-sm btn-info" title="View" data-bs-toggle="modal" data-bs-target="#fileAttachmentViewModal" onclick="FileAttachmentView.setupView(\''+curFileId+'\');">'+Icons.view+'</button></td>'));
					fileShowContent.append(nextRow);
				}
			});
		});

		fileObjViewContainerJq.show();

	}
};