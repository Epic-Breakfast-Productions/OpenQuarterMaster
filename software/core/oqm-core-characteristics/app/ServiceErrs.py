import json
import os
from dataclasses import dataclass, field

from fastapi import \
	HTTPException
from starlette.responses import StreamingResponse

from .Shared import CachedImage, ImageUtils


class ServiceErrs:
	@classmethod
	def get_service_err_return(cls) -> str:
		enabled = os.getenv('CHARACTERISTICS_SERVICE_ERR_PAGE_ENABLED', "false") == "true"

		if not enabled:
			raise HTTPException(status_code=404, detail="Service error page is disabled")

		return """
		<!DOCTYPE html>
		<html>
		<head>
			<title>Service Error</title>
		</head>
		<body>
			<h1>Service Error</h1>
			<p>An error occurred while processing your request.</p>
			<button onClick="window.location.reload();">Refresh Page</button>
			<br />
			<br />

			<input type="checkbox" id="countdown" name="countdownToRefresh" value="Bike">
			<label for="countdown">
				Countdown to Refresh after <span id="countdownIndicator"></span> seconds
			</label>
			<script>
				const countdownCheck = document.querySelector('#countdown');
				const countdownElement = document.querySelector('#countdownIndicator');

				let countdownTime = 10.00;//must be in seconds
				let interval = countdownTime / 20;

				countdownElement.textContent = countdownTime;

				const countdownInterval = setInterval(() => {

					if(countdownCheck.checked){
						countdownTime -= interval;
					}

					if (countdownTime <= 0) {
						window.location.reload();
					} else {
						countdownElement.textContent = countdownTime;
					}
				}, interval);
			</script>
		</body>
		</html>
		"""

