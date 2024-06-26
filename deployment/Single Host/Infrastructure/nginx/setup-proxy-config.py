#!/bin/python3
import logging
import os
import json
import sys
from html.parser import HTMLParser
sys.path.append("/usr/lib/oqm/station-captain/")
from ConfigManager import *

CONFIG_TEMPLATE_FILE = "/etc/oqm/serviceConfig/infra/nginx/default.conf.template"
PROXY_CONFIG_DIR = "/etc/oqm/proxyConfig.d"
RESULT_CONFIG_FILE = "/tmp/oqm/serviceConfig/infra/nginx/config.d/default.conf"

INDEX_TEMPLATE_FILE = "/etc/oqm/serviceConfig/infra/nginx/indexTemplate.html"
INDEX_FILE = "/etc/oqm/serviceConfig/infra/nginx/webroot/index.html"

logging.info("Adding proxy config to nginx config.")

upstreamText = """
# Upstreams. Added via script
"""

locationText = """
    # Locations for upstreams. Added via script
"""

coreLinks = ""
infraLinks = ""

for curProxyConfigFile in os.listdir(PROXY_CONFIG_DIR):
    if curProxyConfigFile.endswith(".json"):
        with open(PROXY_CONFIG_DIR + "/" + curProxyConfigFile, 'r') as stream:
            curProxyConfig = json.load(stream)

        curProxyConfig["path"] = "/" + curProxyConfig["type"] + "/" + curProxyConfig["name"] + "/"

        if "host" not in curProxyConfig:
            curProxyConfig["host"] = mainCM.getConfigVal(curProxyConfig["hostConfig"])
        if "port" not in curProxyConfig:
            curProxyConfig["port"] = mainCM.getConfigVal(curProxyConfig["portConfig"])

        curProxyConfig["upstreamName"] = curProxyConfig["host"]

        upstreamText += ("""
upstream {upstreamName} {{
    server {upstreamHost}:{upstreamPort};
}}

        """.format(
            upstreamName=curProxyConfig["upstreamName"],
            upstreamHost=curProxyConfig["host"],
            upstreamPort=curProxyConfig["port"]
        ))

        locationText += """
    location {path} {{
        proxy_pass http://{upstreamName}/;
        #proxy_redirect http://{upstreamName}/ /;

        # tell backend that message compression not allowed
        proxy_set_header Accept-Encoding "";

        sub_filter_types *;
        sub_filter 'action="/'  'action="{path}';
        sub_filter 'href="/'  'href="{path}';
        sub_filter 'src="/'  'src="{path}';
        sub_filter 'action="./'  'action="{path}';
        sub_filter 'href="./'  'href="{path}';
        sub_filter 'src="./'  'src="{path}';
        sub_filter_once off;

        add_header Pragma "no-cache";
        add_header Cache-Control "no-cache";

        include  /etc/nginx/mime.types;
        include /etc/nginx/conf.other.d/proxy_params;
    }}

""".format(
            path=curProxyConfig["path"],
            upstreamName=curProxyConfig["upstreamName"]
        )
        if "showInProxyRoot" not in curProxyConfig or curProxyConfig["showInProxyRoot"]:
            if curProxyConfig["type"] == "infra":
                infraLinks += """
                <li><a href="{path}">{title}</a></li>            
                """.format(
                    path=curProxyConfig["path"],
                    title=curProxyConfig["title"]
                )
            if curProxyConfig["type"] == "core":
                coreLinks += """
                <li><a href="{path}">{title}</a></li>            
                """.format(
                    path=curProxyConfig["path"],
                    title=curProxyConfig["title"]
                )
    else:
        continue

logging.info("Done reading in proxy config files.")

with open(CONFIG_TEMPLATE_FILE, "r") as f:
    configData = f.read()
configData = configData.format(upstreamText=upstreamText, locationText=locationText)
os.makedirs(os.path.dirname(RESULT_CONFIG_FILE), exist_ok=True)
with open(RESULT_CONFIG_FILE, "w") as f:
    f.write(configData)
logging.info("Finished writing new config file.")

with open(INDEX_TEMPLATE_FILE, "r") as f:
    index = f.read()
index = index.format(coreLinks=coreLinks, infraLinks=infraLinks)
os.makedirs(os.path.dirname(INDEX_FILE), exist_ok=True)
with open(INDEX_FILE, "w") as f:
    f.write(index)
logging.info("Finished writing new index file.")
