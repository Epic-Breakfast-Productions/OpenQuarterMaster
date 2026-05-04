import os
from dataclasses import dataclass

import yaml
from fastapi import HTTPException

from .Shared import CachedImage, ImageUtils


@dataclass()
class RunByReturn:
    name: str|None
    email: str|None
    phone: str|None
    website: str|None
    hasLogoImg: bool = False
    hasBannerImg: bool = False

@dataclass()
class BannerReturn:
    text: str|None
    textColor: str|None
    backgroundColor: str|None

@dataclass()
class CharacteristicsReturn:
    title: str|None
    motd: str|None
    runBy: RunByReturn | None
    banner: BannerReturn | None

@dataclass()
class RunByCache:
    name: str|None
    email: str|None
    phone: str|None
    website: str|None
    logoImg: CachedImage|None
    bannerImg: CachedImage|None

    def to_response(self) -> RunByReturn|None:
        return RunByReturn(
            name=self.name,
            email=self.email,
            phone=self.phone,
            website=self.website,
            hasLogoImg=self.logoImg is not None,
            hasBannerImg=self.bannerImg is not None
        )

@dataclass()
class BannerCache:
    text: str|None
    textColor: str|None
    backgroundColor: str|None

    def toResponse(self) -> BannerReturn|None:
        if not self.textColor and not self.backgroundColor and not self.text:
            return None

        return BannerReturn(
            text=self.text,
            textColor=self.textColor,
            backgroundColor=self.backgroundColor
        )

@dataclass()
class CharacteristicsCache:
    title: str|None
    motd: str|None
    runBy: RunByCache | None
    banner: BannerCache | None

    def to_response(self) -> CharacteristicsReturn:
        return CharacteristicsReturn(
            title=self.title,
            motd=self.motd,
            runBy=self.runBy.to_response(),
            banner=self.banner.toResponse()
        )

class CharacteristicsUtils:
    characteristics_file = os.getenv('CHARACTERISTICS_FILE', '/data/characteristics.yaml')

    @classmethod
    def __get_banner(cls, characteristicsYaml) -> BannerCache | None:
        if 'banner' not in characteristicsYaml:
            return None
        return BannerCache(
            os.getenv('CHARACTERISTICS_VAL_RUNBY_TEXT', characteristicsYaml['banner']['text']),
            os.getenv('CHARACTERISTICS_VAL_RUNBY_TEXTCOLOR', characteristicsYaml['banner']['textColor']),
            os.getenv('CHARACTERISTICS_VAL_RUNBY_BACKGROUNDCOLOR', characteristicsYaml['banner']['backgroundColor']),
        )

    @classmethod
    def __get_run_by(cls, characteristicsYaml) -> RunByCache | None:
        if 'runBy' not in characteristicsYaml:
            return None
        return RunByCache(
            os.getenv('CHARACTERISTICS_VAL_RUNBY_NAME', characteristicsYaml['runBy']['name']),
            os.getenv('CHARACTERISTICS_VAL_RUNBY_EMAIL', characteristicsYaml['runBy']['email']),
            os.getenv('CHARACTERISTICS_VAL_RUNBY_PHONE', characteristicsYaml['runBy']['phone']),
            os.getenv('CHARACTERISTICS_VAL_RUNBY_WEBSITE', characteristicsYaml['runBy']['website']),
            ImageUtils.get_image(os.getenv('CHARACTERISTICS_VAL_RUNBY_LOGOIMG', characteristicsYaml['runBy']['logoImg'])),
            ImageUtils.get_image(os.getenv('CHARACTERISTICS_VAL_RUNBY_BANNERIMG', characteristicsYaml['runBy']['bannerImg'])),
        )


    @classmethod
    def get_characteristics_cache(cls)->CharacteristicsCache:
        characteristicsYaml = yaml.safe_load(open(cls.characteristics_file))

        if not isinstance(characteristicsYaml, dict):
            print("ERROR: Characteristics could not be loaded from file: " + characteristicsYaml)
            raise HTTPException(status_code=500, detail="Characteristics could not be loaded.")

        output = CharacteristicsCache(
            os.getenv('CHARACTERISTICS_VAL_TITLE', characteristicsYaml['title']),
            os.getenv('CHARACTERISTICS_VAL_MOTD', characteristicsYaml['motd']),
            cls.__get_run_by(characteristicsYaml),
            cls.__get_banner(characteristicsYaml)
        )

        return output

    @classmethod
    def get_characteristics_return(cls)->CharacteristicsReturn:
        return cls.get_characteristics_cache().to_response()
