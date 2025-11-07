package com.oqm.chest.tracker;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import tech.ebp.oqm.lib.core.api.java.OqmCoreApiClient;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(OQMChestTracker.MODID)
public class OQMChestTracker {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "oqmchesttracker";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "oqmchesttracker" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "oqmchesttracker" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "oqmchesttracker" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "oqmchesttracker:example_block", combining the namespace and path
    // public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "oqmchesttracker:example_block", combining the namespace and path
    // public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "oqmchesttracker:example_id", nutrition 1 and saturation 2
    // public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
    //         .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "oqmchesttracker:example_tab" for the example item, that is placed after the combat tab
    // public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
    //         .title(Component.translatable("itemGroup.oqmchesttracker")) //The language key for the title of your CreativeModeTab
    //         .withTabsBefore(CreativeModeTabs.COMBAT)
    //         .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
    //         .displayItems((parameters, output) -> {
    //             output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
    //         }).build());

    public Map<String, Integer> openChestData = new HashMap<>();
    public Map<String, Integer> closeChestData = new HashMap<>();

    private static OqmCoreApiClient oqmCoreApiClient;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public OQMChestTracker(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (OQMChestTracker) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
//        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

//        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
//            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
//        }
//
//        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());
//
//        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
//    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(EXAMPLE_BLOCK_ITEM);
//        }
//    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    private Map<String, Integer> collectChestItems(ChestMenu chestMenu) {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < chestMenu.getContainer().getContainerSize(); i++) {
            var item = chestMenu.getContainer().getItem(i);
            if (!item.isEmpty()) {
                String name = item.getHoverName().getString();
                int count = item.getCount();
                map.put(name, map.getOrDefault(name, 0) + count);
            }
        }

        return map;
    }

    @SubscribeEvent
    public void onChestOpened(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ChestMenu chestMenu) {
            openChestData = collectChestItems(chestMenu);
        }
    }

    @SubscribeEvent
    public void onChestClosed(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof ChestMenu chestMenu) {
            // Clear previous close chest data
            closeChestData.clear();

            // Get player who closed the chest
            Player player = event.getEntity();
            String playerName = player.getName().getString();

            // Get chest contents
            var container = chestMenu.getContainer();

            // Find the position of the chest
            Object pos;
            if (container instanceof ChestBlockEntity chestBlockEntity) {
                pos = chestBlockEntity.getBlockPos();
            } else {
                pos = "unknown position";
            }

            // Log each item in the chest
            for (int i = 0; i < container.getContainerSize(); i++) {
                var item = container.getItem(i);
                if (!item.isEmpty()) {
                    String name = item.getHoverName().getString();
                    int count = item.getCount();
                    closeChestData.put(name, closeChestData.getOrDefault(name, 0) + count);
                }
            }

            // Compare open and close chest data to find changes
            if (!(openChestData.equals(closeChestData))) {
                // Create a set of all item names in both open and close data
                Set<String> allItemNames = new HashSet<>(openChestData.keySet());
                allItemNames.addAll(closeChestData.keySet());

                // Check for added or removed items
                for (String itemName : allItemNames) {
                    int openCount = openChestData.getOrDefault(itemName, 0);
                    int closeCount = closeChestData.getOrDefault(itemName, 0);
                    if (openCount != closeCount) {
                        int difference = closeCount - openCount;
                        if (difference > 0) {
                            LOGGER.info("Player {} added {} of item {} to chest at {}", playerName, difference, itemName, pos);
                        } else {
                            LOGGER.info("Player {} removed {} of item {} from chest at {}", playerName, -difference, itemName, pos);
                        }
                    }
                }
            } else {
                LOGGER.info("No changes detected in chest contents by player {}", playerName);
            }
        }
    }
}
