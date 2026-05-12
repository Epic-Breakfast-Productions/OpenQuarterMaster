package com.oqm.chest.tracker;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> SERVER_IP = BUILDER
            .comment("Unique OQM server IP")
            .define("serverIp", "127.0.0.1");

    public static final ModConfigSpec.ConfigValue<String> CLIENT_ID = BUILDER
            .comment("Unique OQM Client ID")
            .define("clientId", "");

    public static final ModConfigSpec.ConfigValue<String> CLIENT_SECRET = BUILDER
            .comment("Unique OQM Client Secret")
            .define("clientSecret", "");


    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
