/**
 * Script to handle theme changing.
 */
const PageTheme = {
	mode: "auto",
	lightness: "",

	setMode: function(mode){
		this.mode = mode;

		switch (PageTheme.mode) {
			case "light":
				PageTheme.lightness = "light";
				break;
			case "dark":
				PageTheme.lightness = "dark";
				break;
		}
	},
	isDarkMode: function(){
		return PageTheme.lightness === "dark";
	}
};

(() => {
	'use strict'

	const getStoredTheme = () => localStorage.getItem('theme');
	const setStoredTheme = theme => localStorage.setItem('theme', theme);

	const getPreferredTheme = () => {
		const storedTheme = getStoredTheme();
		if (storedTheme) {
			return storedTheme;
		}
		return "auto";
		// return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
	}

	const setTheme = theme => {
		if (theme === 'auto' && window.matchMedia('(prefers-color-scheme: dark)').matches) {
			PageTheme.setMode("dark");
			document.documentElement.setAttribute('data-bs-theme', 'dark');
		} else {
			PageTheme.setMode(theme);
			document.documentElement.setAttribute('data-bs-theme', theme);
		}
	}

	setTheme(getPreferredTheme());

	const showActiveTheme = (theme, focus = false) => {
		const themePicker = document.querySelector('#theme-picker');
		const themeSwitcherButton = themePicker.querySelector('#bd-theme');

		const themeSwitcherText = themePicker.querySelector('#bd-theme-text');
		const activeThemeIcon = themePicker.querySelector('.theme-icon-active');
		const btnToActive = themePicker.querySelector(`[data-bs-theme-value="`+theme+`"]`);
		const svgOfActiveBtn = btnToActive.querySelector('.theme-icon').innerHTML;

		themePicker.querySelectorAll('[data-bs-theme-value]').forEach(element => {
			element.classList.remove('active');
			element.setAttribute('aria-pressed', 'false');
		})

		btnToActive.classList.add('active');
		btnToActive.setAttribute('aria-pressed', 'true');
		activeThemeIcon.innerHTML = svgOfActiveBtn;
		const themeSwitcherLabel = themeSwitcherText.textContent+` (`+btnToActive.dataset.bsThemeValue+`)`;
		themeSwitcherButton.setAttribute('aria-label', themeSwitcherLabel);

		if (focus) {
			themeSwitcherButton.focus();
		}
		console.log("Set theme to " + theme);
	}

	window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
		const storedTheme = getStoredTheme();
		if (storedTheme !== 'light' && storedTheme !== 'dark') {
			setTheme(getPreferredTheme());
		}
	})

	window.addEventListener('DOMContentLoaded', () => {
		let preferredTheme = getPreferredTheme();
		console.log("Preferred theme: " + preferredTheme);
		showActiveTheme(preferredTheme);

		document.querySelectorAll('[data-bs-theme-value]')
			.forEach(toggle => {
				toggle.addEventListener('click', () => {
					const theme = toggle.getAttribute('data-bs-theme-value')
					setStoredTheme(theme);
					setTheme(theme);
					showActiveTheme(theme, true);
				})
			})
	})
})()
