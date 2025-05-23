import os
import shutil
from ConfigManager import *
from ServiceUtils import *
from OtherUtils import *
from CronUtils import *
import docker
from LogUtils import *


class ContainerUtils:
    """
    https://docker-py.readthedocs.io/en/stable/index.html
    """
    log = LogUtils.setupLogger("ContainerUtils")
    CRON_NAME = "prune-container-resources"
    DOCKER_NW_NAME = "oqm-internal"

    @classmethod
    def setupArgParser(cls, subparsers):
        containerSubparser = subparsers.add_parser("container", help="Container related commands")
        containerSubparsers = containerSubparser.add_subparsers(dest="containerCommand")

        prune_parser = containerSubparsers.add_parser("prune-resources", aliases=["prune"], help="Prunes all unused container resources. Roughly equivalent to running both `docker system prune --volumes` and `docker image prune -a`")
        prune_parser.set_defaults(func=ContainerUtils.pruneContainerResourcesFromArgs)

        ecs_parser = containerSubparsers.add_parser("ensure-setup", help="Ensures all container based resources (i.e, network) are setup and ready.")
        ecs_parser.set_defaults(func=ContainerUtils.ensureSharedDockerResourcesFromArgs)

    @classmethod
    def pruneContainerResourcesFromArgs(cls, args):
        message = cls.pruneContainerResources()
        print("Done pruning container resources. Reclaimed " + message)

    @classmethod
    def ensureSharedDockerResourcesFromArgs(cls, args):
        cls.ensureSharedDockerResources()

    @classmethod
    def pruneContainerResources(cls) -> str:
        cls.log.info("Pruning all container resources.")
        client = docker.from_env()

        numBytesCleared = 0

        numBytesCleared += client.containers.prune()['SpaceReclaimed']
        numBytesCleared += client.images.prune()['SpaceReclaimed']
        numBytesCleared += client.volumes.prune()['SpaceReclaimed']
        # doesn't actually return num bytes reclaimed. Might change in future?
        client.networks.prune()

        output = OtherUtils.human_size(numBytesCleared)
        cls.log.info("Done pruning container resources. Reclaimed %s", output)

        return output

    @staticmethod
    def enableAutomatic():
        CronUtils.enableCron(
            ContainerUtils.CRON_NAME,
            "oqm-captain container prune-resources",
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
        ContainerUtils.log.info("Ensuring docker network exists.")
        client = docker.from_env()

        try:
            client.networks.get(ContainerUtils.DOCKER_NW_NAME)
            ContainerUtils.log.info("Network already present!")
        except Exception as e:
            # TODO:: narrow exception
            ContainerUtils.log.info("Need to create network.")
            client.networks.create(ContainerUtils.DOCKER_NW_NAME)
            ContainerUtils.log.info("Created network.")


