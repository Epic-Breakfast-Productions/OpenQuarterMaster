package tech.ebp.oqm.baseStation.service.mongo.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * TODO:: figure out how to do this with gridfs https://www.mongodb.com/docs/drivers/java/sync/v4.3/fundamentals/gridfs/
 */
@Slf4j
@ApplicationScoped
public class FileAttachmentService extends MongoHistoriedFileService<FileAttachment, FileAttachmentSearch> {
	
	FileAttachmentService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	FileAttachmentService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		TempFileService tempFileService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			FileAttachment.class,
			false,
			tempFileService,
			new FileAttachmentObjectService(
				objectMapper,
				mongoClient,
				database
			)
		);
		((FileAttachmentObjectService)this.getFileObjectService()).setFileService(this);
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, FileAttachment newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
	}
	
	
	private static class FileAttachmentObjectService extends MongoHistoriedObjectService<FileAttachment, FileAttachmentSearch> {
		
		@Setter
		@Getter(AccessLevel.PRIVATE)
		private FileAttachmentService fileService;
		
		FileAttachmentObjectService() {//required for DI
			super(null, null, null, null, null, null, false, null);
		}
		
		FileAttachmentObjectService(
			ObjectMapper objectMapper,
			MongoClient mongoClient,
			String database
		) {
			super(
				objectMapper,
				mongoClient,
				database,
				FileAttachment.class,
				false
			);
//			this.fileService = fileService;
			//        this.validator = validator;
		}
		
		//TODO:: make this work somehow
//		@Override
//		public FindIterable<FileAttachment> listIterator(ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
//			return super.listIterator(clientSession, filter, sort, pageOptions).map(
//				(FileAttachment a)->{
//					return FileAttachmentGet.fromFileAttachment(a, this.getFileService().getRevisions(a.getId()));
//				}
//			);
//		}
	}
}
