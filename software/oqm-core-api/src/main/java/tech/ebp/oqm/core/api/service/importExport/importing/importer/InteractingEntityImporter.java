package tech.ebp.oqm.core.api.service.importExport.importing.importer;

import com.mongodb.client.ClientSession;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.rest.dataImportExport.EntityImportResult;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;
import tech.ebp.oqm.core.api.service.importExport.importing.options.InteractingEntityMapStrategy;
import tech.ebp.oqm.core.api.service.mongo.InteractingEntityService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class InteractingEntityImporter extends TopLevelImporter<EntityImportResult> {

	@Inject
	@Getter(AccessLevel.PRIVATE)
	InteractingEntityService interactingEntityService;


	@Override
	public Path getObjectDirPath(Path topLevelPath) {
		return topLevelPath.resolve(this.interactingEntityService.getCollectionName());
	}

	@Override
	public EntityImportResult readInObjectsImpl(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity,
		DataImportOptions options
	) throws IOException {
		Path entityDirectory = directory.resolve(this.interactingEntityService.getCollectionName());
		EntityImportResult.Builder resultBuilder = EntityImportResult.builder();
		List<InteractingEntity> existentUsers = interactingEntityService.listIterator().into(new ArrayList<>());
		Set<ObjectId> existentUserIds = existentUsers.stream().map(MainObject::getId).collect(Collectors.toSet());
		Map<String, ObjectId> existentEmailsToIds = existentUsers.stream().collect(Collectors.toMap(InteractingEntity::getEmail, MainObject::getId));
		Map<String, ObjectId> existentNamesToIds = existentUsers.stream().collect(Collectors.toMap(InteractingEntity::getName, MainObject::getId));

		Map<ObjectId, ObjectId> oldToNewIds = new HashMap<>();
		long addedEntityCount = 0;

		for(File curEntityDataFile : getObjectFiles(entityDirectory)){
			InteractingEntity importingUser = ObjectUtils.OBJECT_MAPPER.readValue(curEntityDataFile, InteractingEntity.class);
			//if we already have this exact user
			if(existentUserIds.contains(importingUser.getId())){
				continue;
			}

			ObjectId hit = null;
			if(options.getInteractingEntityMapStrategies().contains(InteractingEntityMapStrategy.EMAIL)){
				hit = existentEmailsToIds.get(importingUser.getEmail());
			}
			if(hit != null && options.getInteractingEntityMapStrategies().contains(InteractingEntityMapStrategy.NAME)){
				hit = existentNamesToIds.get(importingUser.getName());
			}

			if(hit != null){
				oldToNewIds.put(importingUser.getId(), hit);
				continue;
			}

			this.interactingEntityService.add(importingUser);
			addedEntityCount++;
		}
		resultBuilder.interactingEntitiesMapped(oldToNewIds);
		resultBuilder.num(addedEntityCount);

		return resultBuilder.build();
	}
}
