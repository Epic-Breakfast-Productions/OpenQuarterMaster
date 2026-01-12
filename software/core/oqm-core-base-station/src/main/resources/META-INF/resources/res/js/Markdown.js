/**
 * https://marked.js.org/
 * https://github.com/cure53/DOMPurify
 *
 * @type {{Parsing: {parseMarkdown: function(*): *}}}
 */
const Markdown = {
	Parsing: {
		parseMarkdown: function (markdown) {
			return DOMPurify.sanitize(
				marked.parse(
					markdown.replace(/^[\u200B\u200C\u200D\u200E\u200F\uFEFF]/, "")
				)
			);
		},

		displayInDiv: function (markdown, divJq) {
			divJq.html(this.parseMarkdown(markdown));
		}
	}
}