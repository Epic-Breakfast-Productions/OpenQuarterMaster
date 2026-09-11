import os
import shutil
import psutil
import docker
from LogUtils import *
from ConfigManager import *
from ServiceUtils import *


class RamLimitProfile(Enum):
	"""
	Allocation profile.

	slim        – conservative; reserves a larger share of host RAM for
				  the OS and any non-managed services.  Per-container
				  limits skew toward the *minimum*.

	performance – generous; allows containers to claim most of the
				  host RAM.  Per-container limits skew toward the
				  *maximum*.
	"""
	SLIM = "slim"
	PERFORMANCE = "performance"

	@classmethod
	def get_profile(cls) -> 'RamLimitProfile':
		return RamLimitProfile[mainCM.getConfigVal("system.resources.ramLimitProfile")]

	@classmethod
	def get_profile_fraction(cls, profile: 'RamLimitProfile' = None) -> float:
		if profile is None:
			profile = cls.get_profile()

		if profile == RamLimitProfile.SLIM:
			return 0.6
		if profile == RamLimitProfile.PERFORMANCE:
			return 0.8
		raise ValueError("Unknown profile " + str(profile))


class OtherUtils:
	log = LogUtils.setupLogger("OtherUtils")
	_gbConversionFactor = 1024 * 1024 * 1024

	@classmethod
	def setupArgParser(cls, subparsers):
		utilsSubparser = subparsers.add_parser("utils", aliases=["u"], help="Commands for miscellaneous utilities")
		snapshotSubparsers = utilsSubparser.add_subparsers(dest="containerCommand")

		snapshot_parser = snapshotSubparsers.add_parser("calcRamLimit", help="Calculates an appropriate RAM limit based on the system for a service.")
		snapshot_parser.add_argument(
			"--min",
			dest="min", default=None, help="The minimum amount of ram to set as a limit, in Gigabytes"
		)
		snapshot_parser.add_argument(
			"--max",
			 dest="max", default=None, help="The maximum amount of ram to set as a limit, in Gigabytes"
		)
		snapshot_parser.set_defaults(func=cls.serviceRamLimitFromArgs)

	@classmethod
	def serviceRamLimitFromArgs(cls, args):
		success, output = cls.service_ram_limit(min=args.min, max=args.max)

		if success:
			print(format(output, '.3f')+ "G")
		else:
			print("FAILED to calculate RAM limit: " + output, file=sys.stderr)

	@staticmethod
	def human_size(numBytes: int, units=None):
		"""
		Returns a human readable string representation of bytes
		https://stackoverflow.com/questions/1094841/get-human-readable-version-of-file-size
		"""
		if units is None:
			units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB']
		if not units:
			return f"{numBytes}B"
		return str(numBytes) + units[0] if numBytes < 1024 else OtherUtils.human_size(numBytes >> 10, units[1:])

	@classmethod
	def __GBtoB(cls, number: int) -> int:
		return number * cls._gbConversionFactor

	@classmethod
	def __BtoGB(cls, number: int) -> int:
		return number / cls._gbConversionFactor

	@classmethod
	def __normalizeMinMaxInput(cls, mm)->float:
		try:
			return float(mm)
		except ValueError:
			return float(mainCM.getConfigVal(mm))

	@classmethod
	def service_ram_limit(
		cls,
		min: float | str | None = None,
		max: float | str | None = None
	) -> float:
		"""
		:param min: the minimum amount of ram to set as a limit, in Gigabytes
		:param max: the maximum amount of ram to set as a limit, in Gigabytes
		:return:
		"""
		numOqmServices = ServiceUtils.getNumOqmServices()

		if numOqmServices == 0:
			numOqmServices = 1

		profileLimitFraction = RamLimitProfile.get_profile_fraction()
		hostRamB = psutil.virtual_memory().total

		cls.log.info("Service ram calculation numbers: \n\t"
				+ "Min ram to give: " + str(min) + "\n\t"
				+ "Max ram to give: " + str(max) + "\n\t"
				+ "Num OQM services: " + str(numOqmServices) + "\n\t"
				+ "Set profile limit fraction: " + str(profileLimitFraction) + "\n\t"
				+ "Host ram: " + str(hostRamB) + " / " + str(cls.__BtoGB(hostRamB))
				)

		oqmSystemLimit = hostRamB * profileLimitFraction
		fairShareB = oqmSystemLimit / numOqmServices

		calculated = cls.__BtoGB(fairShareB)

		if min is not None:
			min = cls.__normalizeMinMaxInput(min)

			if calculated < min:
				calculated = min
		if max is not None:
			max = cls.__normalizeMinMaxInput(max)

			if calculated > max:
				calculated = max

		return True, calculated
