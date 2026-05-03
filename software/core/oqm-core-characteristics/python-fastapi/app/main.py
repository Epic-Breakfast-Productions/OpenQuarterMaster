import logging

from fastapi import FastAPI

from .Characteristics import CharacteristicsUtils


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
app = FastAPI()


@app.get("/characteristics")
def characteristics_get():
    logger.info("Getting characteristics")
    return CharacteristicsUtils.get_characteristics_return()

@app.get("/characteristics/logo")
def characteristics_get_logo():
    return CharacteristicsUtils.get_image_response(
        CharacteristicsUtils.get_characteristics_cache().runBy.logoImg
    )

@app.get("/characteristics/banner")
def characteristics_get_logo():
    return CharacteristicsUtils.get_image_response(
        CharacteristicsUtils.get_characteristics_cache().runBy.bannerImg
    )
