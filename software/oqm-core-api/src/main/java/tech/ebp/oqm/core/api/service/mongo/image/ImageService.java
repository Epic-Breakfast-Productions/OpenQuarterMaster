package tech.ebp.oqm.core.api.service.mongo.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.config.ImageResizeConfig;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.rest.media.ImageGet;
import tech.ebp.oqm.core.api.rest.file.FileUploadBody;
import tech.ebp.oqm.core.api.rest.search.ImageSearch;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.ItemCategoryService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

import javax.imageio.ImageIO;
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
	
	public ImageService() {//for DI
		this(null);
	}
	
	@Inject
	ImageService(
		HistoryEventNotificationService hens
	) {
		super(
			Image.class,
			"image",
			false,
			hens
		);
		this.allowedMimeTypes = Set.of(
			"image/png",
			"image/jpeg",
			"image/bmp",
			"image/tiff",
			"image/webp",
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
		
		return outputImage;
	}
	
	@Override
	public ObjectId add(ClientSession clientSession, Image fileObject, File origImage, String fileName, InteractingEntity interactingEntity) throws IOException {
		File usingImage;
		if(this.imageResizeConfig.enabled()) {
			usingImage = this.getTempFileService().getTempFile(
				FilenameUtils.removeExtension(fileName) + "-resized",
				FilenameUtils.getExtension(fileName),
				"imageUploads"
			);
			{
				BufferedImage bufferedImage = ImageIO.read(origImage);
				bufferedImage = resizeImage(bufferedImage);
				ImageIO.write(bufferedImage, this.imageResizeConfig.savedType(), usingImage);
			}
		} else {
			usingImage = origImage;
		}
		
		return super.add(clientSession, fileObject, usingImage, fileName, interactingEntity);
	}
	
	@Override
	public CollectionStats getStats() {
		return super.addBaseStats(CollectionStats.builder())
				   .build();
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, Image newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
	}
	
	@Override
	public ImageGet fileObjToGet(Image obj) {
		return ImageGet.fromImage(obj, this.getRevisions(obj.getId()));
	}
	
	@WithSpan
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(ClientSession cs, Image objectToRemove) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(cs, objectToRemove);
		
		Set<ObjectId> refs = this.storageBlockService.getBlocksReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		refs = this.inventoryItemService.getItemsReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		refs = this.itemCategoryService.getItemCatsReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.itemCategoryService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
}
