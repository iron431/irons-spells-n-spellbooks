package io.redspace.skillcasting;

import com.mojang.logging.LogUtils;
import io.redspace.skillcasting.client.SkillcastingClient;
import io.redspace.skillcasting.lifecycle.SkillcastingEvents;
import io.redspace.skillcasting.network.SkillcastingPayloads;
import io.redspace.skillcasting.registry.SkillcastingAttachments;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingResolverTypes;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Bootstrap and shared constants for the standalone skillcasting API.
 *
 * <p>This package ({@code io.redspace.skillcasting.**}) is dependency-isolated: it MUST NOT import
 * any {@code io.redspace.ironsspellbooks.**} type. Wiring flows one way only, from the consuming mod
 * into {@link #init(IEventBus)}. This is the extraction guarantee that lets the package become a
 * standalone library with its own {@code @Mod} entry later.
 */
public final class Skillcasting {
    public static final String NAMESPACE = "skillcasting";
    public static final Logger LOGGER = LogUtils.getLogger();

    private Skillcasting() {
    }

    /**
     * Single entry point for a host mod. Registers all skillcasting registries, the attachment,
     * component types, the payload registrar, and NeoForge lifecycle listeners.
     */
    public static void init(IEventBus modEventBus) {
        SkillcastingResolverTypes.register(modEventBus);
        SkillcastingComponentTypes.register(modEventBus);
        SkillcastingDataComponents.register(modEventBus);
        SkillRegistry.register(modEventBus);
        SkillcastingAttachments.register(modEventBus);

        modEventBus.addListener(SkillcastingRegistries::registerRegistries);
        modEventBus.addListener(SkillcastingPayloads::register);

        NeoForge.EVENT_BUS.register(SkillcastingEvents.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            SkillcastingClient.register(modEventBus, NeoForge.EVENT_BUS);
        }

        LOGGER.debug("Skillcasting API initialized");
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
