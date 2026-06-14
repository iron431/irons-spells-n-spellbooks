package io.redspace.skillcasting.registry;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Holds the custom registry keys and instances for the skillcasting API and registers them on the
 * {@link NewRegistryEvent}, mirroring the mod's {@code SpellRegistry} pattern.
 */
public final class SkillcastingRegistries {
    public static final ResourceKey<Registry<AbstractSkill>> SKILL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Skillcasting.NAMESPACE, "skills"));

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final ResourceKey<Registry<ComponentType<?>>> COMPONENT_TYPE_REGISTRY_KEY =
            (ResourceKey) ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Skillcasting.NAMESPACE, "component_types"));

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final ResourceKey<Registry<PositionResolver.Type<?>>> POSITION_RESOLVER_TYPE_KEY =
            (ResourceKey) ResourceKey.createRegistryKey(Skillcasting.id("position_resolver_types"));

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final ResourceKey<Registry<DirectionResolver.Type<?>>> DIRECTION_RESOLVER_TYPE_KEY =
            (ResourceKey) ResourceKey.createRegistryKey(Skillcasting.id("direction_resolver_types"));

    public static final Registry<AbstractSkill> SKILLS = new RegistryBuilder<>(SKILL_REGISTRY_KEY).create();
    public static final Registry<ComponentType<?>> COMPONENT_TYPES = new RegistryBuilder<>(COMPONENT_TYPE_REGISTRY_KEY).create();
    public static final Registry<PositionResolver.Type<?>> POSITION_RESOLVER_TYPES =
            new RegistryBuilder<>(POSITION_RESOLVER_TYPE_KEY).create();
    public static final Registry<DirectionResolver.Type<?>> DIRECTION_RESOLVER_TYPES =
            new RegistryBuilder<>(DIRECTION_RESOLVER_TYPE_KEY).create();

    public static final Codec<Holder<AbstractSkill>> SKILL_HOLDER_CODEC = SKILLS.holderByNameCodec();
    public static final Codec<ComponentType<?>> COMPONENT_TYPE_CODEC = COMPONENT_TYPES.byNameCodec();

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(SKILLS);
        event.register(COMPONENT_TYPES);
        event.register(POSITION_RESOLVER_TYPES);
        event.register(DIRECTION_RESOLVER_TYPES);
    }
}
