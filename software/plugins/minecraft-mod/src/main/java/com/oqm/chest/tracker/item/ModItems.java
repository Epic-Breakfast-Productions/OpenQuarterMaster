package com.oqm.chest.tracker.item;

import com.oqm.chest.tracker.OQMChestTracker;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OQMChestTracker.MODID);

    public static final DeferredItem<Item> CHEST_PDA = ITEMS.register("chest_pda",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
