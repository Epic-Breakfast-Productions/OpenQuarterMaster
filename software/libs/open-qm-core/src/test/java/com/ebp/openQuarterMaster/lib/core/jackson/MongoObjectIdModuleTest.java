package com.ebp.openQuarterMaster.lib.core.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MongoObjectIdModuleTest {
	
	private final ObjectMapper mapper = new ObjectMapper().registerModule(new MongoObjectIdModule());
	
	@Test
	public void testCanSerializeDeserialize() throws JsonProcessingException {
		ObjectId orig = ObjectId.get();
		
		log.info("Original id: {}", orig);
		
		String idAsJson = mapper.writeValueAsString(orig);
		
		log.info("Id json: {}", idAsJson);
		
		ObjectId deserialized = mapper.readValue(idAsJson, ObjectId.class);
		
		log.info("deserialized: {}", deserialized);
		
		assertEquals(orig, deserialized);
	}
	
	@Test
	public void testCanDeserializeStringQuotes() throws JsonProcessingException {
		ObjectId orig = ObjectId.get();
		
		log.info("Original id: {}", orig);
		
		String idString = "\"" + orig.toHexString() + "\"" ;
		
		
		ObjectId deserialized = mapper.readValue(idString, ObjectId.class);
		
		log.info("deserialized: {}", deserialized);
		
		assertEquals(orig, deserialized);
	}
	
	//can't handle this
//	@Test
//	public void testCanDeserializeStringNoQuotes() throws JsonProcessingException {
//		ObjectId orig = ObjectId.get();
//
//		log.info("Original id: {}", orig);
//
//		String idString = orig.toHexString();
//
//		ObjectId deserialized = mapper.readValue(idString, ObjectId.class);
//
//		log.info("deserialized: {}", deserialized);
//
//		assertEquals(orig, deserialized);
//	}
	
}