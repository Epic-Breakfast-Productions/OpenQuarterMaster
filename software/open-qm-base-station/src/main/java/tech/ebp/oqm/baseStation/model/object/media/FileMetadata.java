package tech.ebp.oqm.baseStation.model.object.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
	
	public static final Tika TIKA = new Tika();
	
	public FileMetadata(File file) throws IOException {
		this(
			file.getName(),
			FilenameUtils.getExtension(file.getName()),
			file.length(),
			FileHashes.fromFile(file),
			TIKA.detect(file),
			ZonedDateTime.now()
		);
	}
	
	
	@NonNull
	@NotNull
	@NotBlank
	private String origName;
	
	@NonNull
	@NotNull
	@NotBlank
	private String fileExtension;
	
	@Positive
	private long length;
	
	@NonNull
	@NotNull
	private FileHashes hashes;
	
	@NotNull
	@NonNull
	@NotBlank
	private String mimeType;
	
	@NotNull
	@NonNull
	@NotBlank
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
