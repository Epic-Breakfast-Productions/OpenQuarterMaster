package tech.ebp.oqm.core.api.service.mongo.image;

import com.mongodb.client.ClientSession;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.ImageResizeConfig;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.ImageSearch;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCategoryService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class ImageService extends MongoHistoriedFileService<Image, FileUploadBody, ImageSearch, ImageGet> {

	@Inject
	ImageResizeConfig imageResizeConfig;
	@Inject
	StorageBlockService storageBlockService;
	@Inject
	ItemCategoryService itemCategoryService;
	@Inject
	InventoryItemService inventoryItemService;
	
	public ImageService() {
		super(
			Image.class,
			"image",
			false
		);
		this.allowedMimeTypes = Set.of(
			"image/png",
			"image/jpeg",
			"image/bmp",
			"image/gif"
		);
	}
	
	/**
	 * Resizes the given image to what should be held in the object.
	 *
	 * @param inputImage The image to resize
	 *
	 * @return The resized image
	 */
	public BufferedImage resizeImage(BufferedImage inputImage) {
		log.debug("Resizing image: {}", inputImage);
		// creates output image
		BufferedImage outputImage = new BufferedImage(
			this.imageResizeConfig.width(),
			this.imageResizeConfig.height(),
			inputImage.getType()
		);
		
		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(
			inputImage,
			0,
			0,
			this.imageResizeConfig.width(),
			this.imageResizeConfig.height(),
			null
		);
		g2d.dispose();

		log.debug("Resized image: {}", outputImage);
		
		return outputImage;
	}
	
	public void writeImage(BufferedImage imageData, File endImage){
		
		if(this.imageResizeConfig.isJpg()) {
			if (imageData.getType() != BufferedImage.TYPE_INT_RGB) {
				log.debug("Converting image to rgb for jpeg writing.");
				BufferedImage rgbImage = new BufferedImage(imageData.getWidth(), imageData.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g = rgbImage.createGraphics();
				g.drawImage(imageData, 0, 0, null);
				g.dispose();
				imageData = rgbImage;
			}
		}
		
		try {
			if(this.imageResizeConfig.isJpg()){
				
				try(FileImageOutputStream out = new FileImageOutputStream(endImage)) {
					//extra setup for higher quality jpg
					final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
					writer.setOutput(out);
					
					ImageWriteParam jpgWriteParam = writer.getDefaultWriteParam();
					jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					jpgWriteParam.setCompressionQuality(this.imageResizeConfig.jsonCompression());
					
					writer.write(null, new IIOImage(imageData, null, null), jpgWriteParam);
					writer.dispose();
				}
			} else {
				if(!ImageIO.write(imageData, this.imageResizeConfig.savedType(), endImage)) {
					throw new IllegalStateException("FAILED to write resized image file.");
				}
			}
		} catch(IOException | IllegalStateException e) {
			throw new RuntimeException("Failed to write image data: " + e.getMessage(), e);
		}
	}
	
	@Override
	public ObjectId add(String oqmDbIdOrName, ClientSession clientSession, Image fileObject, File origImage, String fileName, InteractingEntity interactingEntity) throws IOException {
		File endImage;
		if(this.imageResizeConfig.enabled()) {
			String origFileNameNoExt = FilenameUtils.removeExtension(fileName);

			endImage = this.getTempFileService().getTempFile(
				origFileNameNoExt + "-resized",
				this.imageResizeConfig.savedType(),
				"imageUploads"
			);
			log.info("Image needs resized: {}", origImage);
			{
				BufferedImage bufferedImage = ImageIO.read(origImage);

				if(bufferedImage == null){
					throw new IllegalArgumentException("Image data given was invalid or unsupported.");
				}

				bufferedImage = resizeImage(bufferedImage);
				this.writeImage(bufferedImage, endImage);
			}
		} else {
			endImage = origImage;
		}
		
		return super.add(oqmDbIdOrName, clientSession, fileObject, endImage, fileName, interactingEntity);
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, Image newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
	}
	
	@Override
	public ImageGet fileObjToGet(String oqmDbIdOrName, Image obj) {
		return ImageGet.fromImage(obj, this.getRevisions(oqmDbIdOrName, obj.getId()));
	}
	
	@WithSpan
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(String oqmDbIdOrName, ClientSession cs, Image objectToRemove) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(oqmDbIdOrName, cs, objectToRemove);
		
		Set<ObjectId> refs = this.storageBlockService.getBlocksReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		refs = this.inventoryItemService.getItemsReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		refs = this.itemCategoryService.getItemCatsReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.itemCategoryService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return Image.CUR_SCHEMA_VERSION;
	}
}
