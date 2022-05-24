package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.mongoUtils.AnyMapCodecProvider;
import com.ebp.openQuarterMaster.baseStation.mongoUtils.CustomCodecProvider;
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
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.eclipse.microprofile.config.ConfigProvider;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MongoTestConnector {
	
	private final static MongoTestConnector INSTANCE = new MongoTestConnector();
	
	public static MongoTestConnector getInstance() {
		return INSTANCE;
	}
	
	private String mongoConnectionString = null;
	public final String mongoDatabaseName = ConfigProvider.getConfig().getValue("quarkus.mongodb.database", String.class);
	
	private String getMongoConnectionString(){
		if(this.mongoConnectionString == null){
			this.mongoConnectionString = ConfigProvider.getConfig().getValue("quarkus.mongodb.connection-string", String.class);
		}
		return this.mongoConnectionString;
	}
	
	private static CodecRegistry getRegistry() {
		CodecRegistry registry = CodecRegistries.fromRegistries(
			MongoClientSettings.getDefaultCodecRegistry(),
			CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
		);
		
		registry = CodecRegistries.fromProviders(
			registry,
			new CustomCodecProvider()
		);
		registry = CodecRegistries.fromRegistries(
			registry,
			CodecRegistries.fromProviders(new AnyMapCodecProvider())
		);
		
		registry = CodecRegistries.fromRegistries(
			registry,
			MongoClientSettings.getDefaultCodecRegistry()
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
			MongoDatabase db = client.getDatabase(this.mongoDatabaseName);
			
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
		log.info("Finished clearing database. Deleted {} records.", totalDeleted);
	}
	
}
