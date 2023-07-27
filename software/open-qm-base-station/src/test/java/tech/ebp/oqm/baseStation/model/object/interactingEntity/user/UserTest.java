package tech.ebp.oqm.baseStation.model.object.interactingEntity.user;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.testUtils.BasicTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test image facts:
 * <p>
 * - center color: FF0000 - TL quad:      00FF00 - TR quad:      0000FF - BL quad:      000000 - TR quad:      FFFF00
 */
@Slf4j
class UserTest extends BasicTest {
	
	@Test
	public void getEntityReferenceTest() {
		User user = new User();
		user.setId(new ObjectId());
		
		InteractingEntityReference ref = user.getReference();
		
		assertNotNull(ref);
		assertNotNull(ref.getEntityId());
		assertNotNull(ref.getEntityType());
		assertEquals(InteractingEntityType.USER, ref.getEntityType());
		assertEquals(user.getId(), ref.getEntityId());
		
		log.info("Reference: {}", ref);
	}
	
}