var wholeBody = $('body');

function doRestCall(
	{
		spinnerContainer = wholeBody.get(0),
		url = null,
		timeout = (5 * 60 * 1000),
		method = 'GET',
		data = null,
		authorization = false,
		extraHeaders = {},
		async = true,
		crossDomain= false,
		done,
		fail = function () {
		},
		failNoResponse = null,
		failNoResponseCheckStatus = true,
	} = {}
) {
	console.log("Making rest call to " + url);
	var spinner = (spinnerContainer === null ? null : new Spin.Spinner(spinnerOpts).spin(spinnerContainer));

	var ajaxOps = {
		url: url,
		method: method,
		timeout: timeout,
		async: async,
		crossDomain: crossDomain
	};

	if (data != null) {
		if( data instanceof FormData){
			ajaxOps.cache= false;
			ajaxOps.contentType= false;
			ajaxOps.processData= false;
			ajaxOps.data = data;
			console.log("Sending form data.");
		} else {
			ajaxOps.contentType = "application/json; charset=UTF-8";
			ajaxOps.dataType = 'json';
			ajaxOps.data = JSON.stringify(data);
			console.log("Sending json data: " + ajaxOps.data);
		}
	}

	if (authorization) {
		extraHeaders = {
			...extraHeaders,
			...{Authorization: "Bearer " + authorization}
		}
	}
	if(crossDomain){
		extraHeaders = {
			...extraHeaders,
			...{"Access-Control-Allow-Origin":"*"}
		}
	}

	ajaxOps.headers = extraHeaders;

	$.ajax(
		ajaxOps
	).done(function (data, status, xhr) {
		console.log("Got successful response from "+url+" (trace id: "+xhr.getResponseHeader("traceId")+"): " + JSON.stringify(data));
		done(data);
	}).fail(function (data, status, statusStr) {
		console.warn("Request failed to "+url+" (trace id: "+data.getResponseHeader("traceId")+"): " + JSON.stringify(data));

		var response = data.responseJSON;

		if (data.status == 0) { // no response from server
			if (failNoResponseCheckStatus) {
				//getServerStatus();
			}
			console.info("Failed due to lack of connection to server.");
			if (failNoResponse != null) {
				failNoResponse(data);
			} else {
				addMessage(
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
}