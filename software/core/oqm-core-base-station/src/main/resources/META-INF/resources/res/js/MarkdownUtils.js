import "../../lib/DOMPurify/3.3.1/DOMPurify.min.js";
import "../../lib/Marked/17.0.1/marked.min.js";
import "../../lib/overtype/2.3.4/overtype.min.js";

/**
 * https://marked.js.org/
 * https://github.com/cure53/DOMPurify
 *
 * @type {{Parsing: {parseMarkdown: function(*): *}}}
 */
export class MarkdownUtils {
	static Parsing = class {
		/**
		 * Parses the given markdown string and returns the sanitized html.
		 * @param markdown The markdown string to parse.
		 * @returns {*} The sanitized html parsed from the markdown string.
		 */
		static parseMarkdown(markdown) {
			return DOMPurify.sanitize(
				marked.parse(
					markdown.replace(/^[\u200B\u200C\u200D\u200E\u200F\uFEFF]/, "")
				)
			);
		}

		static displayInDiv(markdown, divJq) {
			divJq.html(this.parseMarkdown(markdown));
		}
	}
	static Editor = class {
		/**
		 * https://github.com/panphora/overtype?tab=readme-ov-file#options
		 */
		static inputDefaults = {
			toolbar: true,
			autoResize: true,      // Auto-expand height with content
			minHeight: '100px',     // Minimum height when autoResize is enabled
			maxHeight: '500px',
			theme: PageTheme.isDarkMode()? "cave" : "solar"
		}
		/**
		 * Initialize the markdown editor.
		 * @param editorDivJq
		 * @returns {*} The OverType instance.
		 */
		static initInput(editorSelector){
			console.log("Initializing markdown editor for element ", editorSelector);

			return OverType.initFromData(
				editorSelector,
				MarkdownUtils.Editor.inputDefaults
			);
		}
	}
}
