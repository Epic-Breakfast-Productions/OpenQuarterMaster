Configuring the Server
======================


Characteristics
---------------

The characteristics are loaded in via a file specified by the ``CHARACTERISTICS_FILE`` environment var, or ``/data/characteristics.yaml`` by default.

The values can be overridden by associated environment variables, also indicated in the below example.

Schema of this file, all fields optional, except where noted:

.. code-block:: yaml

	# What to call this instance
	#    CHARACTERISTICS_VAL_TITLE
	title:

	# A message to display
	#    CHARACTERISTICS_VAL_MOTD
	motd:

	# Information directly about who's running the system
	runBy:
	  # The name of the group running the system
	  #    CHARACTERISTICS_VAL_RUNBY_NAME
	  name:
	  # The email of the group running the system
	  #    CHARACTERISTICS_VAL_RUNBY_EMAIL
	  email:
	  # The phone number of the group running the system
	  #    CHARACTERISTICS_VAL_RUNBY_PHONE
	  phone:
	  # The website of the group running the system
	  #    CHARACTERISTICS_VAL_RUNBY_WEBSITE
	  website:
	  # The path to where to load the logo image *
	  #    CHARACTERISTICS_VAL_RUNBY_LOGOIMG
	  logoImg:
	  # The path to where to load the banner image *
	  #    CHARACTERISTICS_VAL_RUNBY_BANNERIMG
	  bannerImg:

	# Defines a banner to be displayed at the top of the screen. Example would be a classification marking. All fields are mandatory if specifying a banner.
	banner:
	  # The text to display.
	  #    CHARACTERISTICS_VAL_BANNER_TEXT
	  text:
	  # The color of the text +
	  #    CHARACTERISTICS_VAL_BANNER_TEXTCOLOR
	  textColor:
	  # The color of the background +
	  #    CHARACTERISTICS_VAL_BANNER_BACKGROUNDCOLOR
	  backgroundColor:

``*`` = Paths can be given either in full (``/path/to/file.jpg``), or in a directory specified by ``CHARACTERISTICS_RUNBY_IMG_DIR``.

``+`` = Colors are specified by either names or hex (``#000000``) values. Any value considered valid by the HTML color spec is to be considered valid.

UI's
----

UIs are defined by files listed in the directory specified by ``CHARACTERISTICS_UI_DIR``, or ``/data/uis/`` by default.

These UI entries are intended to provide navigation between web interfaces.

Format of each file:

.. code-block:: json

	{
	   "type": "Core",
	   "order": 0,
	   "id": "oqm-core-base_station",
	   "name": "Base Station",
	   "description": "The Main UI for Open QuarterMaster. If you are unsure where to start, start here!",
	   "url": "http://foo",
	   "urlConfigKey": "core.baseStation.externalBaseUri",
	   "icon": "/core/api/core-api.svg",
	   "monitorEndpoint": "/q/health",
	   "endpoints": {
		  "item": {
			 "view": "/items?item={item}"
		  }
	   }
	}

Field information:

 * ``type``: The type of service/ ui. Options:

   * "``Core``", or "``Plugin``"
 * ``order``: Where this service falls in the order of the list of services. Lower is higher.
 * ``id``: the specific identifier for this service. Used to identify the service entry.
 * ``url``: the url of the service.


Homepage
........

The service also presents the system's homepage uri. Set this with ``UIS_HOME_URL``
