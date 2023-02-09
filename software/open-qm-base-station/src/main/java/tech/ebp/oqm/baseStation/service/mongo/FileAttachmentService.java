package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.lib.core.object.media.file.FileAttachment;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * TODO:: figure out how to do this with gridfs https://www.mongodb.com/docs/drivers/java/sync/v4.3/fundamentals/gridfs/
 */
@Traced
@Slf4j
@ApplicationScoped
public class FileAttachmentService extends MongoHistoriedFileService<FileAttachment, FileAttachmentSearch> {
	
	FileAttachmentService() {//required for DI
		super(null, null, null, null, null, null, false, null, null);
	}
	
	@Inject
	FileAttachmentService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		CodecRegistry codecRegistry
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			FileAttachment.class,
			false,
			codecRegistry
		);
	}
	
	
	@Override
	public void ensureObjectValid(boolean newObject, FileAttachment newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
	}
	
}
