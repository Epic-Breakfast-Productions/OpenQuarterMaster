class ScriptInfo:
    SCRIPT_VERSION = 'SCRIPT_VERSION'
    SCRIPT_PACKAGE_NAME = "open+quarter+master-manager-station+captain"
    SCRIPT_TITLE = "Open QuarterMaster Station Captain V" + SCRIPT_VERSION
    # urls
    HOME_GIT = "https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster"
    STATION_CAPT_GIT = HOME_GIT + "/tree/main/software/Station%20Captain"
    HOME_PLUGIN_REPO = "https://raw.githubusercontent.com/Epic-Breakfast-Productions/OpenQuarterMaster/main/software/plugins/plugin-repo.json"
    GIT_API_BASE = "https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster"
    GIT_RELEASES = GIT_API_BASE + "/releases"
    # files
    TMP_DIR = "/tmp/oqm"
    DOWNLOAD_DIR = TMP_DIR + "/download"
    DATA_DIR = "/data/oqm"
    SHARED_CONFIG_DIR = "/etc/oqm"
    META_INFO_DIR = SHARED_CONFIG_DIR + "/meta"
    CONFIG_DIR = SHARED_CONFIG_DIR + "/config"
    CONFIG_VALUES_DIR = CONFIG_DIR + "/configs"
    SERVICE_CONFIG_DIR = SHARED_CONFIG_DIR + "/serviceConfig"
    SNAPSHOT_SCRIPTS_LOC = SHARED_CONFIG_DIR + "/snapshot/scripts"

    CONFIG_DEFAULT_UPDATE_FILE = "99-main.json"


class ConfigKeys:
    # Email
    CONFIG_KEY_EMAIL_PREFIX = "system.emailSend"
    CONFIG_KEY_EMAIL_FROM = CONFIG_KEY_EMAIL_PREFIX + ".fromAddress"
    CONFIG_KEY_EMAIL_HOST = CONFIG_KEY_EMAIL_PREFIX + ".host"
    CONFIG_KEY_EMAIL_PORT = CONFIG_KEY_EMAIL_PREFIX + ".sslPort"
    CONFIG_KEY_EMAIL_USER = CONFIG_KEY_EMAIL_PREFIX + ".username"
    CONFIG_KEY_EMAIL_PASS = CONFIG_KEY_EMAIL_PREFIX + ".password"

