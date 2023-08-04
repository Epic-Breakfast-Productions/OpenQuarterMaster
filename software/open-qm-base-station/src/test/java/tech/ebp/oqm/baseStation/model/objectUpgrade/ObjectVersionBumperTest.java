package tech.ebp.oqm.baseStation.model.objectUpgrade;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class ObjectVersionBumperTest {
	
	@Test
	void testCompareTo() {
		assertEquals(
			0,
			new ObjectVersionBumper<>(2) {
				@Override
				public JsonNode bumpObject(JsonNode node) {
					return null;
				}
			}.compareTo(
				new ObjectVersionBumper<>(2) {
					@Override
					public JsonNode bumpObject(JsonNode node) {
						return null;
					}
				}
			)
		);
		
		assertEquals(
			1,
			new ObjectVersionBumper<>(3) {
				@Override
				public JsonNode bumpObject(JsonNode node) {
					return null;
				}
			}.compareTo(
				new ObjectVersionBumper<>(2) {
					@Override
					public JsonNode bumpObject(JsonNode node) {
						return null;
					}
				}
			)
		);
		
		assertEquals(
			-1,
			new ObjectVersionBumper<>(2) {
				@Override
				public JsonNode bumpObject(JsonNode node) {
					return null;
				}
			}.compareTo(
				new ObjectVersionBumper<>(3) {
					@Override
					public JsonNode bumpObject(JsonNode node) {
						return null;
					}
				}
			)
		);
	}
	
	
	@Test
	void testListOrdering() {
		int start = 1;
		int end = 5;
		TreeSet<ObjectVersionBumper<?>> bumpers = new TreeSet<>();
		
		for(int i = start; i < end; i++){
			bumpers.add(
				new ObjectVersionBumper<>(i) {
					@Override
					public JsonNode bumpObject(JsonNode node) {
						return null;
					}
				}
			);
		}
		
		Iterator<ObjectVersionBumper<?>> it = bumpers.iterator();
		for(int i = start; i < end; i++){
			assertEquals(
				i,
				it.next().getBumperTo()
			);
		}
	}
}
