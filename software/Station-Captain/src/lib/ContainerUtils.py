import logging
import os
import shutil
from ConfigManager import *
from ServiceUtils import *
from OtherUtils import *
from CronUtils import *
import docker


class ContainerUtils:
    """
    https://docker-py.readthedocs.io/en/stable/index.html
    """
    CRON_NAME = "prune-container-resources"

    @staticmethod
    def pruneContainerResources() -> str:
        logging.info("Pruning all container resources.")
        client = docker.from_env()

        numBytesCleared = 0

        numBytesCleared += client.containers.prune()['SpaceReclaimed']
        numBytesCleared += client.images.prune()['SpaceReclaimed']
        numBytesCleared += client.volumes.prune()['SpaceReclaimed']
        # doesn't actually return num bytes reclaimed. Might change in future?
        client.networks.prune()

        output = OtherUtils.human_size(numBytesCleared)
        logging.info("Done pruning container resources. Reclaimed %s", output)

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
