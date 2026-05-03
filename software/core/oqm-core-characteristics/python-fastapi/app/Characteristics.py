import os
from dataclasses import dataclass
from io import BytesIO
from pathlib import Path

import yaml
from PIL import Image
from fastapi import HTTPException
from starlette.responses import StreamingResponse


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
class CharacteristicImage:
    name: str
    type: str
    data: bytes

@dataclass()
class RunByCache:
    name: str|None
    email: str|None
    phone: str|None
    website: str|None
    logoImg: CharacteristicImage|None
    bannerImg: CharacteristicImage|None

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
    characteristics_file = os.getenv('CHARACTERISTICS_FILE', './characteristics.yaml')

    @classmethod
    def __validate_image(cls, imagePathStr:str|None) -> CharacteristicImage|None:
        if not imagePathStr:
            return None
        imagePath = Path(imagePathStr)

        if not imagePath.exists() and imagePath.is_file():
            print(f"ERROR: Image path does not exist: {imagePath}")
            return None

        try:
            with Image.open(imagePath) as image:
                image.verify()
        except (IOError, SyntaxError) as e:
            print(f"ERROR: Image at path does pass verification: {imagePath}")

        try:
            with Image.open(imagePath) as image:
                mimetype = image.get_format_mimetype()
                img_byte_arr = BytesIO()
                image.save(img_byte_arr, format=mimetype.split('/')[1])

                return CharacteristicImage(
                    os.path.basename(imagePathStr),
                    mimetype,
                    img_byte_arr.getvalue()
                )
        except (IOError, SyntaxError) as e:
            print(f"ERROR: Image at path does pass verification: {imagePath}")




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
            cls.__validate_image(os.getenv('CHARACTERISTICS_VAL_RUNBY_LOGOIMG', characteristicsYaml['runBy']['logoImg'])),
            cls.__validate_image(os.getenv('CHARACTERISTICS_VAL_RUNBY_BANNERIMG', characteristicsYaml['runBy']['bannerImg'])),
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

    @classmethod
    def get_image_response(cls, image: CharacteristicImage|None)->StreamingResponse:
        if not image:
            raise HTTPException(status_code=400, detail="No logo image has been provided to serve.")

        return StreamingResponse(
            BytesIO(image.data),
            media_type=image.type,
            headers={
                "Content-Disposition": "attachment;filename=" + image.name
            }
        )