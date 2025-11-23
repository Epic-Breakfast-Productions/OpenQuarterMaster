package com.oqm.chest.tracker;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import tech.ebp.oqm.lib.core.api.java.OqmCoreApiClient;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.config.KeycloakConfig;
import tech.ebp.oqm.lib.core.api.java.search.QueryParams;
import tech.ebp.oqm.lib.core.api.java.utils.jackson.JacksonUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

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

    public static final OqmCoreApiClient client;

    Map<String, String> itemIdName = new HashMap<>();
    Map<String, String> storageIdName = new HashMap<>();

    static {
        try {
            client = IgnoreCertIssues();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

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

        // testing adding and deleting items and storage blocks
        LOGGER.info("Item id and storage id map contents: ");
        LOGGER.info(itemIdName.toString());
        LOGGER.info(storageIdName.toString());

        // Adding an item and block
        this.addInvItem("startItem", "BULK");
        this.addStorageBlock("startBlock");

        LOGGER.info("Item id and storage id map contents: ");
        LOGGER.info(itemIdName.toString());
        LOGGER.info(storageIdName.toString());

        // Deleting an item and block
        this.deleteInvItem("startItem");
        this.deleteStorage("startBlock");

        LOGGER.info("Item id and storage id map contents: ");
        LOGGER.info(itemIdName.toString());
        LOGGER.info(storageIdName.toString());
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

    // Adds an inventory item
    public void addInvItem(String name, String storeType, String unit) {
        ObjectNode newItem = JacksonUtils.MAPPER.createObjectNode();

        newItem.put("name", name);
        newItem.put("storageType", storeType);
        newItem.putObject("unit").put("string", unit);

        HttpResponse<ObjectNode> response = client.invItemCreate(client.getDefaultCreds(), "default", newItem).join();
        LOGGER.info("status code : {}", Integer.toString(response.statusCode()));
        if (response.statusCode() != 200) {
            LOGGER.info("Attempted to add inventory item, Unexpected response code on : {}", response.statusCode());
        }
        else {
            LOGGER.info("added item with id : {}", response.body().get("id").textValue());
            itemIdName.put(name, response.body().get("id").textValue());
            LOGGER.info(response.body().toPrettyString());
        }

    }

    public void addInvItem(String name, String storeType) {
        ObjectNode newItem = JacksonUtils.MAPPER.createObjectNode();

        newItem.put("name", name);
        newItem.put("storageType", storeType);
        newItem.putObject("unit").put("string", "units");

        HttpResponse<ObjectNode> response = client.invItemCreate(client.getDefaultCreds(), "default", newItem).join();
        LOGGER.info("status code : {}", Integer.toString(response.statusCode()));
        if (response.statusCode() != 200) {
            LOGGER.info("Attempted to add inventory item, Unexpected response code on : {}", response.statusCode());
        }
        else {
            LOGGER.info("added item with id : {}", response.body().get("id").textValue());
            itemIdName.put(name, response.body().get("id").textValue());
            LOGGER.info(response.body().toPrettyString());
        }

    }
    public void addInvItem(String name) {
        ObjectNode newItem = JacksonUtils.MAPPER.createObjectNode();

        newItem.put("name", name);
        newItem.put("storageType", "BULK");
        newItem.putObject("unit").put("string", "units");

        HttpResponse<ObjectNode> response = client.invItemCreate(client.getDefaultCreds(), "default", newItem).join();
        LOGGER.info("status code : {}", Integer.toString(response.statusCode()));
        if (response.statusCode() != 200) {
            LOGGER.info("Attempted to add inventory item, Unexpected response code on : {}", response.statusCode());
        }
        else {
            LOGGER.info("added item with id : {}", response.body().get("id").textValue());
            itemIdName.put(name, response.body().get("id").textValue());
            LOGGER.info(response.body().toPrettyString());
        }
    }

    public void addStorageBlock(String label) {
        ObjectNode newBlock = JacksonUtils.MAPPER.createObjectNode();
        newBlock.put("label", label);

        HttpResponse<ObjectNode> response = client.storageBlockAdd(client.getDefaultCreds(), "default", newBlock).join();
        LOGGER.info("status code : {}", Integer.toString(response.statusCode()));
        if (response.statusCode() != 200) {
            LOGGER.info("Attempted to add storage block, Unexpected response code on : {}", response.statusCode());
        }
        else {
            LOGGER.info("added storage block with id : {}", response.body().get("id").textValue());
            storageIdName.put(label, response.body().get("id").textValue());
            LOGGER.info(response.body().toPrettyString());
        }
    }

    public void deleteInvItem(String name) {

        HttpResponse<ObjectNode> delRes = client.invItemDelete(client.getDefaultCreds(), "default", itemIdName.get(name)).join();
        LOGGER.info("status code : {}", Integer.toString(delRes.statusCode()));
        if (delRes.statusCode() != 200) {
            LOGGER.info("Attempted to delete inventory item : {}", delRes.statusCode());
        }
        else {
            itemIdName.remove(name);
            LOGGER.info("Deleted inventory item with id : {}", delRes.body().get("id").textValue());
            LOGGER.info(delRes.body().toPrettyString());
        }
    }

    public void deleteStorage(String label) {
        HttpResponse<ObjectNode> delRes = client.storageBlockDelete(client.getDefaultCreds(), "default", storageIdName.get(label)).join();
        LOGGER.info("status code : {}", Integer.toString(delRes.statusCode()));
        if (delRes.statusCode() != 200) {
            LOGGER.info("Attempted to delete storage block : {}", delRes.statusCode());
        }
        else {
            storageIdName.remove(label);
            LOGGER.info("Deleted stoarage block with id : {}", delRes.body().get("id").textValue());
            LOGGER.info(delRes.body().toPrettyString());
        }
    }

    private static OqmCoreApiClient IgnoreCertIssues() throws NoSuchAlgorithmException, KeyManagementException {

        //build SSLContext to ignore cert issues
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(
                null,
                new TrustManager[]{
                        new X509ExtendedTrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[0];
                            }

                            public void checkClientTrusted(
                                    final X509Certificate[] a_certificates,
                                    final String a_auth_type
                            ) {
                            }

                            public void checkServerTrusted(
                                    final X509Certificate[] a_certificates,
                                    final String a_auth_type
                            ) {
                            }

                            public void checkClientTrusted(
                                    final X509Certificate[] a_certificates,
                                    final String a_auth_type,
                                    final Socket a_socket
                            ) {
                            }

                            public void checkServerTrusted(
                                    final X509Certificate[] a_certificates,
                                    final String a_auth_type,
                                    final Socket a_socket
                            ) {
                            }

                            public void checkClientTrusted(
                                    final X509Certificate[] a_certificates,
                                    final String a_auth_type,
                                    final SSLEngine a_engine
                            ) {
                            }

                            public void checkServerTrusted(
                                    final X509Certificate[] a_certificates,
                                    final String a_auth_type,
                                    final SSLEngine a_engine
                            ) {
                            }
                        }
                },
                null
        );

        boolean KcDefaultCreds = true;
        //build the client
        try {
            OqmCoreApiClient _client = OqmCoreApiClient.builder()
                    .httpClient(HttpClient.newBuilder()
                            .sslContext(context)
                            .build())
                    .config(CoreApiConfig.builder()
                            .keycloakConfig(KeycloakConfig.builder().httpClient(HttpClient.newBuilder()
                                            .sslContext(context)
                                            .build())
                                    .baseUri(new URI("https://10.1.6.27/infra/keycloak"))
                                    .clientId("mc-mod")
                                    .clientSecret("HjMxFF3CqiS3qf_XJkJwvKShSdBlLzWl")
                                    .defaultCreds(KcDefaultCreds)
                                    .build())
                            .baseUri(new URI("https://10.1.6.27/core/api")).build())
                    .build();




            HttpResponse<ObjectNode> response = _client.serverHealthGet().join();
            LOGGER.info("client build status code : {}",Integer.toString(response.statusCode()));
            return _client;

        } catch(
                URISyntaxException e) {
            throw new RuntimeException("Failed to create uri for core api.", e);
        }

    }
}
