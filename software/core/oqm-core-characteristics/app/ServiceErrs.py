import os

from fastapi import \
	HTTPException
from jinja2 import Template

from .UIs import UiUtils


class ServiceErrs:
	errorPageContent = None

	@classmethod
	def get_service_err_return(cls) -> str:
		enabled = os.getenv('CHARACTERISTICS_SERVICE_ERR_PAGE_ENABLED', "false") == "true"

		if not enabled:
			raise HTTPException(status_code=404, detail="Service error page is disabled.")

		if not cls.errorPageContent:
			# TODO:: add contact info, if provided
			cls.errorPageContent = Template("""<!DOCTYPE html>
		<html>
		<head>
			<title>Service Error</title>
		</head>
		<style>
		body, button, a {
		font-family: 'Courier New', monospace;
		}
		</style>
		<body>
			<h1>Service Error</h1>
			<p>The service failed to load. It might be down. If the issue persists, please contact the administrators of this instance.</p>
			<p>
				<a href="{{ homeLink }}">Return to Homepage</a>
			</p>

			<br>
			<button onClick="window.location.reload();">&#8634; Retry</button>
			<br />
			<br />

			<input type="checkbox" id="countdown" name="countdownToRefresh" checked>
			<label for="countdown">
				Countdown to automatically &#8634; retry after <span id="countdownIndicator"></span> seconds
			</label>

			<br />
			<br />
			<hr />
			&copy; 2026 <a href="https://epic-breakfast-productions.tech/">Epic Breakfast Productions</a>

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
						countdownElement.textContent = Math.ceil(countdownTime/1000);
					}
				}, interval);
			</script>
		</body>
		</html>
		""").render(
				homeLink=UiUtils.get_uis_cache().home
			)

		return cls.errorPageContent
