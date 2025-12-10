package com.oqm.chest.tracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oqm.chest.tracker.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.*;
import java.util.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import tech.ebp.oqm.lib.core.api.java.OqmCoreApiClient;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.config.KeycloakConfig;
import tech.ebp.oqm.lib.core.api.java.search.QueryParams;
import tech.ebp.oqm.lib.core.api.java.utils.jackson.JacksonUtils;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(OQMChestTracker.MODID)
public class OQMChestTracker {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "oqmchesttracker";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static OqmCoreApiClient client;

    public static final String itemsPath = "..\\src\\main\\java\\com\\oqm\\chest\\tracker\\itemIds.txt";
    public static final String storagePath = "..\\src\\main\\java\\com\\oqm\\chest\\tracker\\storeIds.txt";

    public Map<String, String> itemIdName = new HashMap<>();
    public Map<String, String> storageIdName = new HashMap<>();

    public HashMap<String, Integer> storedItems = new HashMap<>();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public OQMChestTracker(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so items get registered
        ModItems.ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (OQMChestTracker) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CHEST_PDA);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Save maps for next start up NOT SAVE INDEPENDENT
        saveItemMap();
        saveStorageMap();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        String clientId = Config.CLIENT_ID.get();
        String clientSecret = Config.CLIENT_SECRET.get();
        String serverIp = Config.SERVER_IP.get();

        try {
            client = IgnoreCertIssues(serverIp, clientId, clientSecret);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }

        loadItemMap();
        loadStorageMap();

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

        QueryParams params = new QueryParams();
        HttpResponse<ObjectNode> response = client.invItemSearch(client.getDefaultCreds(), "default", params).join();
        LOGGER.info(response.body().toPrettyString());
    }

    public BlockPos getLeftChestPos(BlockState blockState, BlockPos pos) {
        if (!(blockState.getBlock() instanceof ChestBlock)) return pos;

        ChestType type = blockState.getValue(ChestBlock.TYPE);
        Direction direction = blockState.getValue(ChestBlock.FACING);

        if (type == ChestType.LEFT) {
            // This IS the left chest
            return pos;
        }

        if (type == ChestType.RIGHT) {
            // Move one block to the left relative to chest facing
            Direction leftDir = direction.getCounterClockWise();
            return pos.relative(leftDir);
        }

        // SINGLE chest, left side is itself
        return pos;
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        // clear stored items map
        storedItems.clear();

        int count;
        int currentCount;
        Set<String> items = new HashSet<>(Set.of());
        Set<String> removedItems = new HashSet<>(Set.of());

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();
        BlockState blockState = level.getBlockState(pos);

        if (level.isClientSide()) return;
        if (!player.isCrouching()) return;
        if (!(blockState.getBlock() instanceof ChestBlock chest)) return;
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chestBlockEntity)) return;
        if (stack.getItem() != ModItems.CHEST_PDA.get()) return;

        Container container = ChestBlock.getContainer(chest, blockState, level, pos, true);

        if (container == null) return;

        CompoundTag data = chestBlockEntity.getPersistentData();
        if (data.contains("items")) {
            data.getString("items");
            items = new HashSet<>(Arrays.asList(data.getString("items").split(",")));
        }

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty()) {
                String name = item.getHoverName().getString();

                items.add(name);

                count = item.getCount();
                currentCount = storedItems.getOrDefault(name, 0);
                storedItems.put(name, currentCount + count);
            }
        }

        items.removeIf(name -> {
            boolean removed = !storedItems.containsKey(name);
            if (removed) removedItems.add(name);
            return removed;
        });

        data.putString("items", String.join(",", items));

        BlockPos logPos = getLeftChestPos(blockState, pos);

        LOGGER.info(storedItems.toString());
        LOGGER.info(logPos.toString());
        player.displayClientMessage(Component.literal("Updated Chest with OQM"), true);
        for (String removed : removedItems) {
            deleteInvItem(removed);
        }

        for (Map.Entry<String, Integer> entry : storedItems.entrySet()) {

            changeItemValue(entry.getKey(), logPos.toString(), entry.getValue());
        }
    }

    // Adds an inventory item
    public void addInvItem(String name, String storeType) {
        ObjectNode newItem = JacksonUtils.MAPPER.createObjectNode();

        newItem.put("name", name);
        newItem.put("storageType", storeType);
        // newItem.putObject("unit").put("string", unit);

        HttpResponse<ObjectNode> response = client.invItemCreate(client.getDefaultCreds(), "default", newItem).join();
        LOGGER.info("status code : {}", Integer.toString(response.statusCode()));
        if (response.statusCode() != 200) {
            LOGGER.info("Attempted to add inventory item, Unexpected response code on : {}", response.statusCode());
            LOGGER.info(response.body().toPrettyString());
        }
        else {
            LOGGER.info("added item with id : {}", response.body().get("id").textValue());
            itemIdName.put(name, response.body().get("id").textValue());
            LOGGER.info(response.body().toPrettyString());
        }
    }

    public void addInvItem(String name, String storeType, String location) {
        ObjectNode newItem = JacksonUtils.MAPPER.createObjectNode();
        newItem.put("name", name);
        newItem.put("storageType", storeType);

        newItem.putArray("storageBlocks").add(storageIdName.get(location));
        //newItem.putObject("attributes").put("location", location);
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
            LOGGER.info("Attempted to delete inventory item : {}", name);
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

    public void changeItemValue(String name, String location, int value ) {
        if (!storageIdName.containsKey(location)) {
            addStorageBlock(location);
        }
        if (!itemIdName.containsKey(name)) {
            addInvItem(name, "BULK", location);
        }
        if (!checkStored(name, location)) {
            this.addStoredLocation(name, location);
        }
        storeItems(name, location, value);
    }

    public void removeStoredLocation(String name, String location) {

        ObjectNode itemUpdate = JacksonUtils.MAPPER.createObjectNode();
        Iterator<JsonNode> els = client.invItemGet(client.getDefaultCreds(), "default", itemIdName.get(name)).join()
                .body().withArray("storageBlocks").elements();
        int i = 0;
        while (els.hasNext()) {
            i++;
            if (els.next().textValue() == location) {
                break;
            }
        }
        itemUpdate.withArray("storageBlocks").remove(i);
        HttpResponse<ObjectNode> res = client.invItemUpdate(client.getDefaultCreds(), "default", itemIdName.get(name), itemUpdate).join();
        LOGGER.info("status code : {}", Integer.toString(res.statusCode()));
        LOGGER.info(res.body().toPrettyString());
        if (res.statusCode() != 200) {
            LOGGER.info("Attempted to add storage block, Unexpected response code on : {}", res.statusCode());

        }

    }

    public void addStoredLocation(String name, String location) {

        ObjectNode itemUpdate = JacksonUtils.MAPPER.createObjectNode();
        int arrSize = client.invItemGet(client.getDefaultCreds(), "default", itemIdName.get(name)).join()
                .body().withArray("storageBlocks").size();
        itemUpdate.withArray("storageBlocks").insert(arrSize, storageIdName.get(location));
        HttpResponse<ObjectNode> res = client.invItemUpdate(client.getDefaultCreds(), "default", itemIdName.get(name), itemUpdate).join();
        LOGGER.info("status code : {}", Integer.toString(res.statusCode()));
        LOGGER.info(res.body().toPrettyString());
        if (res.statusCode() != 200) {
            LOGGER.info("Attempted to add storage block, Unexpected response code on : {}", res.statusCode());

        }

    }

    public boolean checkStored(String name, String location) {

        HttpResponse<ObjectNode> aRes = client.invItemGet(client.getDefaultCreds(), "default", itemIdName.get(name)).join();
        LOGGER.info("TEST:");
        //Consumer<? super JsonNode> action;
        boolean isStored = false;
        while (aRes.body().withArray("storageBlocks").elements().hasNext()) {
            if (Objects.equals(aRes.body().withArray("storageBlocks").elements().next().textValue(), storageIdName.get(location))) {
                isStored = true;
                LOGGER.info("TRUE");
                break;
            }
        }
        return isStored;
    }

    public void storeItems(String name, String location, int amount) {
        ObjectNode transaction = JacksonUtils.MAPPER.createObjectNode();
        transaction.put("block", storageIdName.get(location));
        transaction.putObject("amount").put("value", amount)
                .put("scale", "ABSOLUTE")
                .putObject("unit").put("string", "units");
        transaction.put("type", "SET_AMOUNT");

        HttpResponse<String> storeRes = client.invItemStoredTransact(client.getDefaultCreds(), "default", itemIdName.get(name), transaction).join();
        LOGGER.info(storeRes.toString());
        if (storeRes.statusCode() != 200) {
            LOGGER.info("Attempted to update stored item : {}", storeRes.statusCode());
            LOGGER.info(storeRes.body());
        }
        else {

            LOGGER.info("Updated amount of item with id : {}", storeRes.body());
            LOGGER.info(storeRes.body());
        }
    }

    private static OqmCoreApiClient IgnoreCertIssues(String serverIp, String clientId, String clientSecret) throws NoSuchAlgorithmException, KeyManagementException {

        //build SSLContext to ignore cert issues
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(
                null,
                new TrustManager[]{
                        new X509ExtendedTrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
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

        // build uri strings
        String OqmUri = "https://" + serverIp + "/core/api";
        String KcUri = "https://" + serverIp + "/infra/keycloak";

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
                                    .baseUri(new URI(KcUri))
                                    .clientId(clientId)
                                    .clientSecret(clientSecret)
                                    .defaultCreds(KcDefaultCreds)
                                    .build())
                            .baseUri(new URI(OqmUri)).build())
                    .build();




            HttpResponse<ObjectNode> response = _client.serverHealthGet().join();
            LOGGER.info("client build status code : {}",Integer.toString(response.statusCode()));
            return _client;

        } catch(
                URISyntaxException e) {
            throw new RuntimeException("Failed to create uri for core api.", e);
        }

    }

    // saves the item map
    public void saveItemMap() {
        File itemFile = new File(itemsPath);
        BufferedWriter bf = null;
        try{
            bf = new BufferedWriter(new FileWriter(itemFile));

            // iterate map entries
            for (Map.Entry<String, String> entry :
                    itemIdName.entrySet()) {

                // put key and value separated by a colon
                bf.write(entry.getKey() + ":"
                        + entry.getValue());

                // new line
                bf.newLine();
            }

            bf.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {

            try {

                // always close the writer
                bf.close();
            } catch (Exception e) {
            }
        }
    }

    // saves the storage map
    public void saveStorageMap() {
        File storeFile = new File(storagePath);
        BufferedWriter bf = null;
        try{
            bf = new BufferedWriter(new FileWriter(storeFile));

            // iterate map entries
            for (Map.Entry<String, String> entry :
                    storageIdName.entrySet()) {

                // put key and value separated by a colon
                bf.write(entry.getKey() + ":"
                        + entry.getValue());

                // new line
                bf.newLine();
            }

            bf.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {

            try {

                // always close the writer
                bf.close();
            } catch (Exception e) {
            }
        }
    }

    // Loads the item map from the file
    public void loadItemMap() {
        itemIdName.clear();
        File itemFile = new File(itemsPath);
        BufferedReader bf = null;
        String line = null;
        try {
            bf = new BufferedReader(new FileReader(itemFile));

            while ((line = bf.readLine()) != null) {
                String[] pair = line.split(":", 2);
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = pair[1];
                    itemIdName.put(key, value);
                }
                else {
                    LOGGER.warn("No Key:Value found in line, ignoring: " + line);
                }
            }
            LOGGER.info("items loaded: " + itemIdName.size());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads the storages map from the file
    public void loadStorageMap() {
        storageIdName.clear();
        File itemFile = new File(storagePath);
        BufferedReader bf = null;
        String line = null;
        try {
            bf = new BufferedReader(new FileReader(itemFile));

            while ((line = bf.readLine()) != null) {
                String[] pair = line.split(":", 2);
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = pair[1];
                    storageIdName.put(key, value);
                }
                else {
                    LOGGER.warn("No Key:Value found in line, ignoring: " + line);
                }
            }
            LOGGER.info("storages loaded: " + storageIdName.size());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}