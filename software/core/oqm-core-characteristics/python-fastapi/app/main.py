import logging

from fastapi import FastAPI

from .Characteristics import CharacteristicsUtils
from .Shared import ImageUtils
from .UIs import UiUtils

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
app = FastAPI()


@app.get("/all")
def get_all():
    return {
        "characteristics": CharacteristicsUtils.get_characteristics_return(),
        "uis": UiUtils.get_uis_return()
    }


@app.get("/characteristics")
def characteristics_get():
    logger.info("Getting characteristics")
    return CharacteristicsUtils.get_characteristics_return()

@app.get("/characteristics/logo")
def characteristics_get_logo():
    return ImageUtils.get_image_response(
        CharacteristicsUtils.get_characteristics_cache().runBy.logoImg
    )

@app.get("/characteristics/banner")
def characteristics_get_logo():
    return ImageUtils.get_image_response(
        CharacteristicsUtils.get_characteristics_cache().runBy.bannerImg
    )


@app.get("/uis")
def uis_get():
    return UiUtils.get_uis_return()

@app.get("/uis/{category}/{index}")
def uis_get(category: str, index: int):
    return UiUtils.get_ui_icon(category, index)
