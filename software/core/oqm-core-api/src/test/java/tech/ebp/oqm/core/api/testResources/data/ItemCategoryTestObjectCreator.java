package tech.ebp.oqm.core.api.testResources.data;



import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;

import jakarta.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class ItemCategoryTestObjectCreator extends TestObjectCreator<ItemCategory> {
	private static final AtomicInteger counter = new AtomicInteger(0);
	private static final Random rand = new SecureRandom();
	
	@Override
	public ItemCategory getTestObject() {
		// Java 'Color' class takes 3 floats, from 0 to 1.
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		
		String name =(faker.name().name() + " " + counter.incrementAndGet());
		name = name.substring(0, Math.min(25, name.length()));
		
		ItemCategory itemCategory = new ItemCategory(
			name,
			faker.lorem().paragraph(),
			new Color(r, g, b),
			null
		);
		
		return itemCategory;
	}
}
