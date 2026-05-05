import os
from dataclasses import dataclass
from io import BytesIO
from pathlib import Path

from PIL import Image
from fastapi import HTTPException
from starlette.responses import StreamingResponse


@dataclass()
class CachedImage:
    name: str
    type: str
    data: bytes

class ImageUtils:
    @classmethod
    def get_image(cls, imagePathStr:str|None) -> CachedImage|None:
        if not imagePathStr:
            return None
        imagePath = Path(imagePathStr)

        if not imagePath.exists() or not imagePath.is_file():
            print(f"ERROR: Image file does not exist: {imagePath}")
            return None

        if imagePath.suffix == ".svg":
            with open(imagePath, "rb") as f:
                return CachedImage(
                    os.path.basename(imagePathStr),
                    "image/svg+xml",
                    f.read()
                )
        else:
            try:
                with Image.open(imagePath) as image:
                    image.verify()
            except (IOError, SyntaxError) as e:
                print(f"ERROR: Image at path does not pass verification: {imagePath}")

            try:
                with Image.open(imagePath) as image:
                    mimetype = image.get_format_mimetype()
                    img_byte_arr = BytesIO()
                    image.save(img_byte_arr, format=mimetype.split('/')[1])

                    return CachedImage(
                        os.path.basename(imagePathStr),
                        mimetype,
                        img_byte_arr.getvalue()
                    )
            except (IOError, SyntaxError) as e:
                print(f"ERROR: Image at path does pass verification: {imagePath}")

    @classmethod
    def get_image_response(cls, image: CachedImage|None)->StreamingResponse:
        if not image:
            raise HTTPException(status_code=400, detail="No image has been provided to serve.")

        return StreamingResponse(
            BytesIO(image.data),
            media_type=image.type,
            headers={
                "Content-Disposition": "attachment;filename=" + image.name
            }
        )