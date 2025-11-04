
const Rest = {
	wholeBody: $('body'),
	webroot: Constants.rootPrefix,
	apiRoot: Constants.rootPrefix + "/api",
	passRoot: Constants.rootPrefix + "/api/passthrough",
	componentRoot: Constants.rootPrefix + "/api/pageComponents",
	csrfToken: null,
	getCsrfToken: function () {
		if(Rest.csrfToken == null){
			Rest.csrfToken = $('body').data('csrft');
		}
		return Rest.csrfToken;
	},
	addcsrf(data){
		if(data instanceof FormData){
			data.append("csrf-token", Rest.getCsrfToken());
		}
	},
	buildErrorMessageFromResponse(response, statusMessage){
		let output = "";

		if(
			Object.prototype.hasOwnProperty.call(response, "responseJSON")
			&& (
				Object.prototype.hasOwnProperty.call(response.responseJSON, "displayMessage")
				|| Object.prototype.hasOwnProperty.call(response.responseJSON, "message")
				|| Object.prototype.hasOwnProperty.call(response.responseJSON, "details")
			)
		){
			if(Object.prototype.hasOwnProperty.call(response.responseJSON, "displayMessage")){
				output = response.responseJSON.displayMessage;
			} else if(Object.prototype.hasOwnProperty.call(response.responseJSON, "message")) {
				output = response.responseJSON.message;
			} else {
				output = response.responseJSON.details;
			}
		} else if(
			response.responseText
		){
			output = response.responseText;
		} else {
			output = statusMessage;
		}

		return output;
	},
	/**
	 *
	 * @param spinnerContainer The container to throw the spinner on top of.
	 * @param url Thr URL of the request
	 * @param timeout
	 * @param method
	 * @param data
	 * @param authorization
	 * @param extraHeaders
	 * @param async If this function should await the ajax promise before returning
	 * @param crossDomain
	 * @param returnType
	 * @param done Required, Function to call when the call is successful. Called with (data, status, xhr)
	 * @param fail Optional, Function to call when the call fails
	 * @param failMessagesDiv Null for no auto message display. String Jquery selector or Jquery object otherwise.
	 * @param failNoResponse
	 * @param failNoResponseCheckStatus
	 * @returns {jqXHR} the ajax promise from calling jquery ajax
	 */
	call : async function(
		{
			spinnerContainer = Rest.wholeBody.get(0),
			url = null,
			timeout = (5 * 60 * 1000),
			method = 'GET',
			data = null,
			authorization = false,
			extraHeaders = {},
			async = true,
			crossDomain = false,
			returnType = "json",
			done = function(){
				console.debug("No actions specified.");
			},
			fail = function() {},
			failMessagesDiv = null,
			failNoResponse = null,
			failNoResponseCheckStatus = true,
			csrt = Rest.getCsrfToken()
		} = {}
	) {
		console.log("Making "+ method +" rest call to " + url);
		let spinner = (spinnerContainer === null ? null : new Spin.Spinner(spinnerOpts).spin(spinnerContainer));

		let ajaxOps = {
			url: url,
			method: method,
			timeout: timeout,
			dataType: returnType
		};

		if (data != null) {
			if (data instanceof FormData) {
				ajaxOps.cache = false;
				ajaxOps.contentType = false;
				ajaxOps.processData = false;

				Rest.addcsrf(data);

				ajaxOps.data = data;

				console.log("Sending form data.");
			} else {
				ajaxOps.contentType = "application/json; charset=UTF-8";
				ajaxOps.dataType = 'json';
				ajaxOps.data = JSON.stringify(data);
				console.log("Sending json data: ", data);
			}
		}

		// if (authorization) {
		// 	extraHeaders = {
		// 		...extraHeaders,
		// 		...{Authorization: "Bearer " + authorization}
		// 	}
		// }
		if (crossDomain) {
			ajaxOps = {
				...ajaxOps,
				...{CORS: true}
			}
			extraHeaders = {
				...extraHeaders,
				...{'Access-Control-Allow-Origin': "*"},
			}
		}

		if(csrt){
			extraHeaders = {
				...extraHeaders,
				...{'X-CSRF-TOKEN': csrt}
			}
		}

		ajaxOps.headers = extraHeaders;

		console.log("Calling with headers: ", ajaxOps.headers);
		console.debug("Full request object: ", ajaxOps);

		let ajaxPromise = $.ajax(ajaxOps)
			.done(function (data, status, xhr) {
				console.log("Got successful response from " + url + " (trace id: " + xhr.getResponseHeader("traceId") + "): ", data);
				try{
					done(data, status, xhr);
				} catch (e){
					console.error("Done func failed: " + e);
					throw e;
				}
			}).fail(function (data, status, statusStr) {
				console.warn("Request failed to " + url + " (trace id: " + data.getResponseHeader("traceId") + ")(status: "+status+", message: "+statusStr+"): ",  data);

				if(failMessagesDiv != null){
					if(String(failMessagesDiv) === failMessagesDiv){
						failMessagesDiv = $(failMessagesDiv);
					}
					PageMessages.addMessageToDiv(
						failMessagesDiv,
						"danger",
						Rest.buildErrorMessageFromResponse(data, statusStr),
						"Action Failed",
						null,
						JSON.stringify(data)
					);
				}

				var response = data.responseJSON;
				if (data.status == 0) { // no response from server
					if (failNoResponseCheckStatus) {
						//getServerStatus();
					}
					console.info("Failed due to lack of connection to server.");
					if (failNoResponse != null) {
						failNoResponse(data);
					} else {
						PageMessages.addMessage(
							"danger",
							"Try refreshing the page, or wait until later. Contact the server operators for help and details.",
							"Failed to connect to server."
						);
					}
				} else {
					fail(data);
				}
			}).always(function () {
				if (spinner != null) {
					spinner.stop();
				}
			});

		if (!async) {
			await ajaxPromise;
		}
		return ajaxPromise;
	}
}
