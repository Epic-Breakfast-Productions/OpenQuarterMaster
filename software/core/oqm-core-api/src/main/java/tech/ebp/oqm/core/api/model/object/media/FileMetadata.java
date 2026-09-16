package tech.ebp.oqm.core.api.model.object.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
	
	public FileMetadata(File file) throws IOException {
		this(
			file.getName(),
			FilenameUtils.getExtension(file.getName()),
			file.length(),
			FileHashes.fromFile(file),
			FileUtils.TIKA.detect(file),
			ZonedDateTime.now()
		);
	}
	
	@NonNull
	@NotNull
	@NotBlank
	@Schema(description = "The original name of the file, as it was uploaded.")
	private String origName;
	
	@NonNull
	@NotNull
	@NotBlank
	@Schema(description = "The original extension of the file.")
	private String fileExtension;
	
	@NotNull
	@Positive
	@Schema(description = "The length of the file in bytes.")
	private long length;
	
	@NonNull
	@NotNull
	@Schema(description = "The hashes of the file.")
	private FileHashes hashes;
	
	@NotNull
	@NonNull
	@NotBlank
	@Schema(description = "The detected mime type of the file.")
	private String mimeType;
	
	@NotNull
	@NonNull
	@NotBlank
	@Schema(description = "The date and time the file was uploaded.")
	private ZonedDateTime uploadDateTime;
	
	public Document toDocument(Codec<FileMetadata> codec) {
		BsonDocument outDoc = new BsonDocument();
		BsonWriter writer = new BsonDocumentWriter(outDoc);
		
		codec.encode(
			writer,
			this,
			EncoderContext.builder().build()
		);
		
		return new Document(outDoc);
	}
	
	public static FileMetadata fromDocument(Document document, Codec<FileMetadata> codec){
		BsonReader reader = new BsonDocumentReader(document.toBsonDocument());
		DecoderContext context = DecoderContext.builder().build();
		return codec.decode(reader, context);
	}
}
