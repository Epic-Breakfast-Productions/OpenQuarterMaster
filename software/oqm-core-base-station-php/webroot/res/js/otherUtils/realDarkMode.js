const RealDarkMode = {
	mouseMove(event, options) {
		const x = event.clientX + window.scrollX;
		const y = event.clientY + window.scrollY;

		const spotlight = document.querySelector(".real-dark-mode-spotlight");

		if (!spotlight) return;

		spotlight.style.background = `radial-gradient(circle at ${x}px ${y}px, transparent ${
			options?.size * options?.falloff || 50
		}px, ${options?.color || "#000000"} ${options?.size || 100}px)`;
	},
	removeRealDarkMode() {
		document.body.classList.remove("real-dark-mode-enabled");
		document.body.removeChild(
			document.querySelector(".real-dark-mode-spotlight")
		);
		document.removeEventListener("mousemove", this.mouseMove);
	},
	createSpotlightElement(options) {
		const spotlight = document.createElement("div");

		if (!spotlight) return;

		spotlight.classList.add("real-dark-mode-spotlight");
		spotlight.style.position = "absolute";
		spotlight.style.top = "0";
		spotlight.style.left = "0";
		spotlight.style.width = "100%";
		spotlight.style.height = "100vh";
		spotlight.style.zIndex = "9999";
		spotlight.style.pointerEvents = "none";
		spotlight.style.opacity = `${options?.opacity || 0.95}`;
		return spotlight;
	},
	realDarkMode(options = {
					 color: "#000000",
					 size: 100,
					 falloff: 0.5,
					 opacity: 0.95,
				 }) {
		if (!document.body) return;

		if (document.body.classList.contains("real-dark-mode-enabled")) {
			console.log("Turning off the dark");
			this.removeRealDarkMode();
			return;
		}
		console.log("Turning on the dark");

		const spotlightElement = this.createSpotlightElement(options);

		if (!spotlightElement) return;

		document.body.appendChild(spotlightElement);
		document.body.classList.add("real-dark-mode-enabled");

		document.addEventListener("mousemove", (event) => this.mouseMove(event, options));
		document.addEventListener("scroll", () =>
			document.querySelector(".real-dark-mode-spotlight").remove()
		);
	}
}

RealDarkMode.realDarkMode();