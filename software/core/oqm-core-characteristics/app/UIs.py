import json
import os
from dataclasses import dataclass, field

from fastapi import \
	HTTPException
from starlette.responses import StreamingResponse

from .Shared import CachedImage, ImageUtils


@dataclass()
class UiEndpoints:
	health: str | None
	item: dict | None


@dataclass()
class UiResponse:
	order: int|None
	name: str
	id: str
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
			order=self.order,
			name=self.name,
			id=self.id,
			description=self.description,
			baseUri=self.baseUri,
			icon=self.icon is not None,
			endpoints=self.endpoints
		)


@dataclass()
class UisResponse:
	home: str
	core: list[UiResponse] = field(default_factory=list)
	plugin: list[UiResponse] = field(default_factory=list)
	metrics: list[UiResponse] = field(default_factory=list)
	infra: list[UiResponse] = field(default_factory=list)


@dataclass()
class UisCache:
	home: str
	core: list[UiCache] = field(default_factory=list)
	plugin: list[UiCache] = field(default_factory=list)
	metrics: list[UiCache] = field(default_factory=list)
	infra: list[UiCache] = field(default_factory=list)

	def to_response(self) -> UisResponse:
		return UisResponse(
			self.home,
			list(map(lambda c: c.to_response(), self.core)),
			list(map(lambda c: c.to_response(), self.plugin)),
			list(map(lambda c: c.to_response(), self.metrics)),
			list(map(lambda c: c.to_response(), self.infra)),
		)

	def getCategory(self, category: str):
		if category == "core":
			return self.core
		if category == "plugin":
			return self.plugin
		if category == "metrics":
			return self.metrics
		if category == "infra":
			return self.infra
		raise ValueError(category)


class UiUtils:
	ui_dir = os.getenv('CHARACTERISTICS_UI_DIR', '/data/uis/')
	ui_cache = None
	ui_cache_force_refresh = False

	@classmethod
	def __get_ui_endpoints(cls, uiRaw) -> UiEndpoints:
		return UiEndpoints(
			uiRaw["monitorEndpoint"],
			uiRaw["item"] if "item" in uiRaw else None,
		)

	@classmethod
	def __get_ui(cls, uiRaw) -> UiCache:

		if "url" not in uiRaw:
			cls.ui_cache_force_refresh = True

		return UiCache(
			uiRaw["order"] if "order" in uiRaw else 999,
			uiRaw["id"] if "id" in uiRaw else None,
			uiRaw["name"],
			uiRaw["description"],
			uiRaw["url"] if "url" in uiRaw else "",
			ImageUtils.get_image(
				uiRaw["icon"],
				os.getenv('CHARACTERISTICS_UIS_ICON_DIR', "/")
			) if "icon" in uiRaw else None,
			cls.__get_ui_endpoints(uiRaw)
		)

	@classmethod
	def validate_ui_list(cls, uiList: list[UiCache]) -> None:
		# ensure id uniqueness
		for uiCache in uiList:
			if not uiCache.id:
				continue
			filtered_list = list(
				filter(
					lambda
						x: uiCache.id == x.id,
					uiList
					)
				)
			if len(filtered_list) != 1:
				raise Exception("Invalid UI ID (duplicates): " + uiCache.id)

	@classmethod
	def get_uis_cache(cls) -> UisCache:
		if cls.ui_cache is not None and not cls.ui_cache_force_refresh:
			return cls.ui_cache

		directory = os.getenv('UIS_DATA_DIR', "/data/uis/")

		uisRawData: list[dict] = list()

		for file in os.listdir(directory):
			filename = os.path.basename(file)
			if filename.endswith(".json"):
				curUiFileLoc = os.path.join(directory, filename)
				with open(curUiFileLoc) as curUiFile:
					uisRawData.append(json.load(curUiFile))

		output: UisCache = UisCache(os.getenv('UIS_HOME_URL', " "))

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

		cls.validate_ui_list(output.core)
		cls.validate_ui_list(output.metrics)
		cls.validate_ui_list(output.infra)
		cls.validate_ui_list(output.plugin)

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
	def get_ui_icon(cls, category: str, uiId: str) -> StreamingResponse:
		try:
			ui = list(
				filter(
					lambda
						x: uiId == x.id,
					cls.get_uis_cache().getCategory(category)
				)
			)
		except ValueError as e:
			raise HTTPException(status_code=404, detail="Invalid UI category: " + category)

		if len(ui) == 0:
			raise HTTPException(status_code=404, detail="Invalid UI ID: " + uiId)
		ui = ui[0]

		return ImageUtils.get_image_response(ui.icon)
