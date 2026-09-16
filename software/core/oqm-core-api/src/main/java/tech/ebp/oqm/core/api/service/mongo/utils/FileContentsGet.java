package tech.ebp.oqm.core.api.service.mongo.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;

import java.io.File;

@Data
@AllArgsConstructor
@SuperBuilder
public class FileContentsGet {
	private FileMetadata metadata;
	private File contents;
}
