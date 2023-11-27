import logging
import datetime
import select
import time
import tarfile
from ConfigManager import *
from ServiceUtils import *
import shutil
from systemd import journal


class LogManagement:

    @staticmethod
    def packageLogs() -> (bool, str):

        compressionAlg = mainCM.getConfigVal("snapshots.compressionAlg")
        if all(curAlg not in compressionAlg for curAlg in ["xz", "gz", "bz2"]):
            return False, "Configured compression algorithm was invalid."

        snapshotName = "OQM-log-capture-{}".format(datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"))

        logging.debug("Snapshot name: %s", snapshotName)
        compilingDir = ScriptInfo.TMP_DIR + "/logCaptures/" + snapshotName
        snapshotLocation = mainCM.getConfigVal("snapshots.location") + "/logCaptures"
        logArchiveName = "{}/{}.tar.{}".format(snapshotLocation, snapshotName, compressionAlg)

        success = False
        try:
            try:
                os.makedirs(compilingDir)
                os.makedirs(snapshotLocation, exist_ok=True)
            except Exception as e:
                logging.error("Failed to create directories necessary for snapshot taking: %s", e)
                return False, str(e)

            logging.info("Writing log messages.")
            services = ServiceUtils.getServiceNames()
            for service in services:
                outFileName = compilingDir + "/" + service + ".log"
                logging.info("Logging events for %s to file %s", service, outFileName)
                # TODO:: tis no worky
                with journal.Reader() as j:
                    j.add_match(_SYSTEMD_UNIT=service)
                    # j.seek_head()
                    j.seek_tail()
                    j.get_next()
                    with open(outFileName, "w") as outfile:
                        p = select.poll()
                        p.register(j, j.get_events())
                        while p.poll(5_000):
                            if j.process() != journal.APPEND:
                                continue
                            logging.debug("Got line")
                            for entry in j:
                                if entry['MESSAGE'] != "":
                                    outfile.write(str(entry['__REALTIME_TIMESTAMP']) + ' ' + entry['MESSAGE'] + "\n")
            logging.info("Done writing log messages.")

            logging.info("Archiving snapshot bundle.")
            start = time.time()
            try:
                with tarfile.open(logArchiveName, "x:" + compressionAlg) as tar:
                    tar.add(compilingDir, arcname="")
            except Exception as e:
                logging.error("FAILED to write files to archive: %s", e)
                return False, str(e)
            logging.info("Completed archiving snapshot bundle. Took %s seconds", time.time() - start)
            success = True
            logging.info("Done Performing snapshot.")
            return True, logArchiveName
        finally:
            logging.info("Cleaning up after snapshot operations")

            try:
                if not success:
                    logging.debug("Removing archive file.")
                    if os.path.exists(logArchiveName):
                        os.remove(logArchiveName)
                logging.debug("Removing compiling dir.")
                shutil.rmtree(compilingDir)
                logging.info("Finished cleaning up after snapshot.")
            except Exception as e:
                logging.error("Failed to clean up after performing snapshot operation: %s", e)
