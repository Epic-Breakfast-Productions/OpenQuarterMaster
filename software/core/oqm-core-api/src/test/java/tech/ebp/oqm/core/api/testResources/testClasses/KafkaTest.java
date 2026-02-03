package tech.ebp.oqm.core.api.testResources.testClasses;

import io.smallrye.reactive.messaging.kafka.companion.ConsumerBuilder;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import lombok.NonNull;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

import java.time.Duration;

/**
 * TODO:: adjust for #1080
 */
public interface KafkaTest {
	
	KafkaCompanion getKafkaCompanion();
	
	
	
	
	default void clearKafkaQueues(@NonNull Logger log){
		log.info("Clearing kafka events.");
		try {//sleep to ensure all messages sent
			Thread.sleep(1_000);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		
		
		//doesn't seem to delete anything
//		for(String topic : this.getKafkaCompanion().topics().list()) {
//			TopicDescription desc;
//			try {
//				desc = this.getKafkaCompanion().topics().waitForTopic(topic).await().indefinitely();
//			} catch(UnknownTopicOrPartitionException e) {
//				log.info("Topic {} already deleted?", topic);
//				continue;
//			}
//
//			for(TopicPartition curPartition :
//				desc.partitions().stream().map(partition -> new TopicPartition(topic, partition.partition())).toList()
//			){
//				this.getKafkaCompanion().deleteRecords(curPartition, Long.MAX_VALUE);
//			}
//		}
		
		
		
		
		//error: throws "This server does not host this topic-partition." out of band
		for(String topic : this.getKafkaCompanion().topics().list()){
			TopicDescription desc;
			try {
				desc = this.getKafkaCompanion().topics().waitForTopic(topic).await().indefinitely();
			} catch(UnknownTopicOrPartitionException e){
				log.info("Topic {} already deleted?", topic);
				continue;
			}
			int numPartitions = desc.partitions().size();

			log.info("Recreating topic: {}/{}", topic, numPartitions);

			try {
				this.getKafkaCompanion().topics().delete(topic);
				log.info("Topic deleted: {}/{}", topic, numPartitions);
			} catch(UnknownTopicOrPartitionException e) {
				log.info("Topic {} already deleted.", topic);
			}

			try {
				this.getKafkaCompanion().topics().createAndWait(topic, desc.partitions().size());
			} catch(UnknownTopicOrPartitionException e) {
				log.info("Topic {} already created?", topic);
			}
		}

		


//
//		this.getKafkaCompanion().deleteRecords();
//
//		try(
//			ConsumerBuilder<String, String> cb = this.getKafkaCompanion().consumeStrings()
//		){
//			long count = cb.fromTopics(HistoryEventNotificationService.ALL_EVENT_TOPIC).awaitCompletion(Duration.ofSeconds(5)).count();
//
//			if(log != null){
//				log.info("Cleared {} events.", count);
//			}
//		} catch(AssertionError e) {
//			if(!e.getMessage().contains("No completion (or failure) event received")){
//				throw e;
//			}
//			log.info("No events to clear.");
//		}
	}
}
