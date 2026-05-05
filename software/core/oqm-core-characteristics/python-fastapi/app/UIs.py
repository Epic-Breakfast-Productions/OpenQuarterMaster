import json
import os
from dataclasses import dataclass, field

from starlette.responses import StreamingResponse

from .Shared import CachedImage, ImageUtils


@dataclass()
class UiEndpoints:
    health: str | None
    item: dict | None


@dataclass()
class UiResponse:
    name: str
    description: str
    baseUri: str
    icon: bool
    endpoints: UiEndpoints


@dataclass()
class UiCache:
    order: int | None
    id: str
    name: str
    description: str
    baseUri: str
    icon: CachedImage | None
    endpoints: UiEndpoints

    def to_response(self) -> UiResponse | None:
        return UiResponse(
            name=self.name,
            description=self.description,
            baseUri=self.baseUri,
            icon=self.icon is not None,
            endpoints=self.endpoints
        )


@dataclass()
class UisResponse:
    core: list[UiResponse] = field(default_factory=list)
    plugin: list[UiResponse] = field(default_factory=list)
    metrics: list[UiResponse] = field(default_factory=list)
    infra: list[UiResponse] = field(default_factory=list)


@dataclass()
class UisCache:
    core: list[UiCache] = field(default_factory=list)
    plugin: list[UiCache] = field(default_factory=list)
    metrics: list[UiCache] = field(default_factory=list)
    infra: list[UiCache] = field(default_factory=list)

    def to_response(self) -> UisResponse:
        return UisResponse(
            list(map(lambda c: c.to_response(), self.core)),
            list(map(lambda c: c.to_response(), self.plugin)),
            list(map(lambda c: c.to_response(), self.metrics)),
            list(map(lambda c: c.to_response(), self.infra)),
        )

    def __getitem__(self, key):
        return getattr(self, key)


class UiUtils:
    ui_dir = os.getenv('UI_DIR', '/data/uis/')
    ui_cache = None

    @classmethod
    def __get_ui_endpoints(cls, uiRaw) -> UiEndpoints:
        return UiEndpoints(
            uiRaw["monitorEndpoint"],
            uiRaw["item"] if "item" in uiRaw else None,
        )

    @classmethod
    def __get_ui(cls, uiRaw) -> UiCache:
        return UiCache(
            uiRaw["order"],
            uiRaw["id"] if "id" in uiRaw else None,
            uiRaw["name"],
            uiRaw["description"],
            uiRaw["url"],
            ImageUtils.get_image(
                os.path.join(
                    os.getenv('UIS_ICON_DIR', "/"),
                    uiRaw["icon"].removeprefix("/")
                )
            ) if "icon" in uiRaw else None,
            cls.__get_ui_endpoints(uiRaw)
        )

    @classmethod
    def get_uis_cache(cls) -> UisCache:
        if cls.ui_cache is not None:
            return cls.ui_cache

        directory = os.getenv('UIS_DATA_DIR', "/data/uis/")

        uisRawData: list[dict] = list()

        for file in os.listdir(directory):
            filename = os.path.basename(file)
            if filename.endswith(".json"):
                curUiFileLoc = os.path.join(directory, filename)
                with open(curUiFileLoc) as curUiFile:
                    uisRawData.append(json.load(curUiFile))

        output: UisCache = UisCache()

        for rawUi in uisRawData:
            uiCache = cls.__get_ui(rawUi)

            if rawUi["type"].casefold() == "core":
                output.core.append(uiCache)
            elif rawUi["type"].casefold() == "plugins":
                output.plugin.append(uiCache)
            elif rawUi["type"].casefold() == "metrics":
                output.metrics.append(uiCache)
            elif rawUi["type"].casefold() == "infra":
                output.infra.append(uiCache)
            else:
                print("WARN:: invalid type: " + rawUi["type"].casefold())

        output.core = sorted(output.core, key=lambda c: c.order)
        output.metrics = sorted(output.metrics, key=lambda c: c.order)
        output.infra = sorted(output.infra, key=lambda c: c.order)
        output.plugin = sorted(output.plugin, key=lambda c: c.order)

        cls.ui_cache = output

        return output

    @classmethod
    def get_uis_return(cls) -> UisResponse:
        return cls.get_uis_cache().to_response()

    @classmethod
    def get_ui_icon(cls, category: str, index: int) -> StreamingResponse:
        return ImageUtils.get_image_response(cls.get_uis_cache()[category][index].icon)
