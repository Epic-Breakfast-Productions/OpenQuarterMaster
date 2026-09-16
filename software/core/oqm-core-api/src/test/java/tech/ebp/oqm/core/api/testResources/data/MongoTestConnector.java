package tech.ebp.oqm.core.api.testResources.data;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.eclipse.microprofile.config.ConfigProvider;
import org.testcontainers.DockerClientFactory;
import tech.ebp.oqm.core.api.service.mongo.utils.AnyMapCodecProvider;
import tech.ebp.oqm.core.api.service.mongo.utils.CustomCodecProvider;

import java.util.Arrays;

import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MongoTestConnector {
	
	private static MongoTestConnector INSTANCE;
	
	public static MongoTestConnector getInstance(boolean intTest) {
		if (INSTANCE == null) {
			INSTANCE = new MongoTestConnector(intTest);
		}
		return INSTANCE;
	}
	
	private MongoTestConnector(boolean intTest) {
		this();
		this.intTest = intTest;
	}
	
	private boolean intTest = false;
	private String mongoConnectionString = null;
	public final String mongoDatabaseName = ConfigProvider.getConfig().getValue("quarkus.mongodb.database", String.class);
	
	private String getMongoConnectionString() {
		if (this.mongoConnectionString == null) {
			this.mongoConnectionString = ConfigProvider.getConfig().getValue("quarkus.mongodb.connection-string", String.class);
			
			if (this.intTest) {
				DockerClient dockerClient = DockerClientFactory.instance().client();
				
				Container mongoContainer = dockerClient.listContainersCmd().exec()
												 .stream()
												 .filter(c->c.getLabels().keySet().stream().anyMatch(l->l.startsWith("io.quarkus.devservice")))
												 .filter(c->c.getImage().contains("mongo"))
												 .findFirst().orElseThrow(()->new IllegalStateException("No mongo container found, or more containers existed."));
				
				ContainerPort port = Arrays.stream(mongoContainer.getPorts())
										 .filter(p->p.getPrivatePort() == 27017 && p.getIp().equals("0.0.0.0"))
										 .findFirst().orElseThrow(()->new IllegalStateException("No mongo port found for container " + mongoContainer.getId()));
				
				this.mongoConnectionString = this.mongoConnectionString.replace("27017", port.getPublicPort()+"");
				this.mongoConnectionString = this.mongoConnectionString.replaceFirst("mongodb://.*:", "mongodb://localhost:");
				
				log.info("Reworked Mongo connection string: {}", this.mongoConnectionString);
//				this.mongoConnectionString ="mongodb://" + mongoContainer.getNetworkSettings().getNetworks().values().iterator().next().getIpAddress() + ":" + port.getPrivatePort();
			}
			
			
		}
		return this.mongoConnectionString;
	}
	
	private static CodecRegistry getRegistry() {
		CodecRegistry registry = CodecRegistries.fromRegistries(
			MongoClientSettings.getDefaultCodecRegistry(),
			CodecRegistries.fromProviders(
				new CustomCodecProvider(),
				new AnyMapCodecProvider()
			),
			CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
		);
		
		return registry;
	}
	
	public MongoClient getClient() {
		MongoClientSettings.Builder clientSettingsBuilder = MongoClientSettings.builder();
		
		clientSettingsBuilder = clientSettingsBuilder.applyConnectionString(new ConnectionString(this.getMongoConnectionString()));
		clientSettingsBuilder = clientSettingsBuilder.codecRegistry(getRegistry());
		
		return MongoClients.create(clientSettingsBuilder.build());
	}
	
	public void clearDb() {
		log.info("Clearing database of all entries.");
		long totalDeleted = 0;
		try (MongoClient client = this.getClient()) {
			for (Document curDbDoc : client.listDatabases()) {
				String curDbName = curDbDoc.getString("name");
				
				if (!curDbName.startsWith(this.mongoDatabaseName) && !curDbName.equals(DEFAULT_TEST_DB_NAME)) {
					log.debug("Skipping clearing db: {}", curDbName);
					continue;
				}
				
				log.info("Deleting database {}", curDbName);
				
				MongoDatabase db = client.getDatabase(curDbName);
				
				for (String curCollectionName : db.listCollectionNames()) {
					log.info("Clearing collection {}", curCollectionName);
					MongoCollection<?> collection = db.getCollection(curCollectionName);
					
					long deletedCount = collection.deleteMany(Filters.empty()).getDeletedCount();
					totalDeleted += deletedCount;
					
					log.info("Deleted {} records from {}", deletedCount, curCollectionName);
					
					long numLeft = collection.countDocuments();
					if (numLeft > 0) {
						throw new IllegalStateException(
							"FAILED to clean collection \"" +
							numLeft +
							"\" after tests, " +
							numLeft +
							" records left."
						);
					}
				}
			}
			
		}
		log.info("Finished clearing database. Deleted {} records.", totalDeleted);
	}
	
}
