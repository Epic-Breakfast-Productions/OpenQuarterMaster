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
			raise HTTPException(status_code=404, detail="Service error page is disabled.")

		# TODO:: add homepage link
		# TODO:: add contact info, if provided
		return """<!DOCTYPE html>
		<html>
		<head>
			<title>Service Error</title>
		</head>
		<body>
			<h1>Service Error</h1>
			<p>The service failed to load. It might be down. If the issue persists, please contact the administrators of this instance.</p>
			<br>
			<button onClick="window.location.reload();">Refresh Page</button>
			<br />
			<br />

			<input type="checkbox" id="countdown" name="countdownToRefresh" value="Bike" checked>
			<label for="countdown">
				Countdown to automatically refresh after <span id="countdownIndicator"></span> seconds
			</label>
			<script>
				console.log("Service error page loaded. Starting countdown.");
				const countdownCheck = document.querySelector('#countdown');
				const countdownElement = document.querySelector('#countdownIndicator');

				let countdownTime = 10_000.00;//must be in milliseconds
				let interval = (countdownTime / 20);

				countdownElement.textContent = countdownTime/1000;

				const countdownInterval = setInterval(() => {
					console.log("Iterating on countdown loop.");

					if(countdownCheck.checked){
						countdownTime -= interval;
					}

					if (countdownTime <= 0) {
						countdownElement.textContent = 0;
						countdownCheck.disabled = true;
						window.location.reload();
					} else {
						countdownElement.textContent = countdownTime/1000;
					}
				}, interval);
			</script>
		</body>
		</html>
		"""

