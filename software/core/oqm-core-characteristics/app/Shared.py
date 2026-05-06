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
	def get_image(cls, imagePathStr: str | None, directory: str|None = None) -> CachedImage | None:
		if not imagePathStr:
			return None
		
		imagePath = Path(imagePathStr)
		
		if directory:
			imagePath = Path(
				os.path.join(
					directory,
					imagePathStr.removeprefix("/")
				)
			)
		
		
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
				return None
			
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
				print(f"ERROR: Image at path was not able to be read: {imagePath}")
				return None
	
	@classmethod
	def get_image_response(cls, image: CachedImage | None) -> StreamingResponse:
		if not image:
			raise HTTPException(status_code=400, detail="No image has been provided to serve.")
		
		return StreamingResponse(
			BytesIO(image.data),
			media_type=image.type,
			headers={
				"Content-Disposition": "attachment;filename=" + image.name
			}
		)

class DataUtils:
	
	@classmethod
	def sanitizeInput(cls, envKey: str, value: str|None) -> str|None:
		output = os.getenv(envKey, value)
		
		if output is not None:
			output = output.strip()
		
		if not output:
			return None
		
		return output
	
