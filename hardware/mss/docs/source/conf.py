# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = 'OQM MSS Hardware'
copyright = '2026, Greg Stewart'
author = 'Greg Stewart'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = [
	'sphinxcontrib.mermaid', # https://github.com/mgaitan/sphinxcontrib-mermaid
	'sphinx-jsonschema' # https://sphinx-jsonschema.readthedocs.io/en/latest/directive.html
]

templates_path = ['_templates']
exclude_patterns = []

numbered_sections = True


# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'alabaster'
html_static_path = ['_static']
html_theme_options = {
	"logo": "logo.svg",
	"logo_name": True,
	# 'show_related': True,

	'github_user': 'Epic-Breakfast-Productions',
	'github_repo': 'OpenQuarterMaster',
	'github_banner': True,
	"github_button": False,
	"show_relbars": True,
	# 'github_count': True,
	# 'extra_nav_links': {
	# 	'Index': 'genindex.html',
	# }

	'body_max_width': 'none',
	'page_width': '90%',
}
html_sidebars = {
	'**': [
		'about.html',
		'localtoc.html',
		'navigation.html',
		'searchbox.html',
	]
}

mermaid_dark_theme="forest"
