import os
import shutil
from ConfigManager import *
from ServiceUtils import *
from OtherUtils import *
from CronUtils import *
import docker
from LogUtils import *

log = LogUtils.setupLogger(__name__)

class ContainerUtils:
    """
    https://docker-py.readthedocs.io/en/stable/index.html
    """
    CRON_NAME = "prune-container-resources"
    DOCKER_NW_NAME = "oqm-internal"

    @staticmethod
    def pruneContainerResources() -> str:
        log.info("Pruning all container resources.")
        client = docker.from_env()

        numBytesCleared = 0

        numBytesCleared += client.containers.prune()['SpaceReclaimed']
        numBytesCleared += client.images.prune()['SpaceReclaimed']
        numBytesCleared += client.volumes.prune()['SpaceReclaimed']
        # doesn't actually return num bytes reclaimed. Might change in future?
        client.networks.prune()

        output = OtherUtils.human_size(numBytesCleared)
        log.info("Done pruning container resources. Reclaimed %s", output)

        return output

    @staticmethod
    def enableAutomatic():
        CronUtils.enableCron(
            ContainerUtils.CRON_NAME,
            "oqm-captain --prune-container-resources",
            CronFrequency[mainCM.getConfigVal("system.automaticContainerPruneFrequency")]
        )

    @staticmethod
    def disableAutomatic():
        CronUtils.disableCron(ContainerUtils.CRON_NAME)

    @staticmethod
    def isAutomaticEnabled() -> bool:
        return CronUtils.isCronEnabled(ContainerUtils.CRON_NAME)

    @staticmethod
    def ensureSharedDockerResources():
        log.info("Ensuring docker network exists.")
        client = docker.from_env()

        try:
            client.networks.get(ContainerUtils.DOCKER_NW_NAME)
            log.info("Network already present!")
        except Exception as e:
            # TODO:: narrow exception
            log.info("Need to create network.")
            client.networks.create(ContainerUtils.DOCKER_NW_NAME)
            log.info("Created network.")


