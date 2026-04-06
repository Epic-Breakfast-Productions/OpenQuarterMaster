import platform

from LogUtils import *



class SystemCheckUtils:
    """
    Resources:
     - https://cryptography.io/en/latest/x509/reference/
     - https://stackoverflow.com/questions/54677841/how-do-can-i-generate-a-pkcs12-file-using-python-and-the-cryptography-module
    """
    log = LogUtils.setupLogger("SystemCheckUtils")
    CPUINFO_CONTENT=None
    ARCHITECTURE=None

    @classmethod
    def getCpuInfo(cls)->str:
        if cls.CPUINFO_CONTENT is None:
            try:
                with open("/proc/cpuinfo", 'r', encoding='utf-8') as file:
                    cls.CPUINFO_CONTENT = file.read()
            except Exception as e:
                print(f"An error occurred reading /proc/cpuinfo: {e}")
        return cls.CPUINFO_CONTENT

    @classmethod
    def getCpuArchitecture(cls)->str:
        if cls.ARCHITECTURE is None:
            cls.ARCHITECTURE = platform.machine()
        return cls.ARCHITECTURE

    @classmethod
    def checkSystem(cls):
        output = []

        cls.checkSysHostnameValid(output)
        cls.checkSysIsSupportedArch(output)
        cls.checkSysHasAvx(output)

        return output


    @classmethod
    def checkSysHostnameValid(cls, errArray = [])-> dict[str, str] | None:
        hostname = platform.node()

        if not any(c.isupper() for c in hostname):
            return None

        output = {
            "title": "System hostname contains upper case characters.",
            "level": "WARN",
            "description": "The system's hostname contains upper case characters. If using mdns for hostname and/or self-signed cert mode, this is known to cause issues."
        }
        errArray.append(output)
        return output


    @classmethod
    def sysIsX86_64(cls)-> bool:
        return cls.getCpuArchitecture() == "x86_64"

    @classmethod
    def sysIsArm64(cls)-> bool:
        return cls.getCpuArchitecture() == "aarch64"

    @classmethod
    def checkSysIsSupportedArch(cls, errArray = [])-> dict[str, str] | None:
        if cls.sysIsX86_64() or cls.sysIsArm64():
            return None
        output = {
            "title": "System is not a supported architecture.",
            "level": "SEVERE",
            "description": "Must be x86_64 or armV8."
        }
        errArray.append(output)
        return output

    @classmethod
    def checkSysHasAvx(cls, errArray = [])-> dict[str, str] | None:
        if not cls.sysIsX86_64():
            return None

        if "avx" not in cls.getCpuInfo():
            cls.log.info("AVX not available")
            output = {
                "title": "AVX not available",
                "level": "SEVERE",
                "description": "The AVX instruction set is not available.",
            }
            errArray.append(output)
            return output
        else:
            return None




