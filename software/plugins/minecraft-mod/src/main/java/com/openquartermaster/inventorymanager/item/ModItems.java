package com.openquartermaster.inventorymanager.item;

import com.openquartermaster.inventorymanager.OpenQuartermasterInventoryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {
	// Register a new item
	public static final Item OQM_CHEST = register("oqm_chest", Item::new, new Item.Settings());

	// Method for registering new mod items
	public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
		// Create the item key.
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(OpenQuartermasterInventoryManager.MOD_ID, name));

		// Create the item instance.
		Item item = itemFactory.apply(settings.registryKey(itemKey));

		// Register the item.
		Registry.register(Registries.ITEM, itemKey, item);

		return item;
	}

	// method that exists to be run when
	public static void initialize() {
		// Log output
		OpenQuartermasterInventoryManager.LOGGER.info("registering Mod Items for " + OpenQuartermasterInventoryManager.MOD_ID);

		// Get the event for modifying entries in the ingredients group.
		// And register an event handler that adds our suspicious item to the ingredients group.
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE)
				.register((itemGroup) -> itemGroup.add(ModItems.OQM_CHEST));
	}



}
