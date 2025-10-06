package io.redspace.ironsspellbooks.api.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber
public class SpellConfigManager extends SimpleJsonResourceReloadListener {
    private static final Set<SpellConfigParameter<?>> ALL_TYPES =  new HashSet<>();
    private static boolean registered = false;

    private static void registerConfigParameterTypes() {
        if (!registered) {
            registered = true;
            ALL_TYPES.add(IronConfigParameters.SCHOOL);
            ALL_TYPES.add(IronConfigParameters.MIN_RARITY);
            ALL_TYPES.add(IronConfigParameters.MAX_LEVEL);
            ALL_TYPES.add(IronConfigParameters.ENABLED);
            ALL_TYPES.add(IronConfigParameters.COOLDOWN_IN_SECONDS);
            ALL_TYPES.add(IronConfigParameters.ALLOW_CRAFTING);
            ALL_TYPES.add(IronConfigParameters.MANA_MULTIPLIER);
            ALL_TYPES.add(IronConfigParameters.POWER_MULTIPLIER);
            NeoForge.EVENT_BUS.post(new RegisterConfigParametersEvent(ALL_TYPES::add));
        }
    }

    @SubscribeEvent
    public static void testevent1(ModifyDefaultConfigValuesEvent event) {
        if (event.getSpell().getSchoolType() == SchoolRegistry.BLOOD.get()) {
            event.setConfigValue(IronConfigParameters.SCHOOL, SchoolRegistry.ELDRITCH.get());
        }
    }

    @SubscribeEvent
    public static void testevent2(RegisterConfigParametersEvent event) {
        event.register(new SpellConfigParameter<Boolean>(ResourceLocation.withDefaultNamespace("test"), Codec.BOOL, false));
    }

    public SpellConfigManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_spellbooks/spell_config");
    }

    private static ImmutableMap<AbstractSpell, SpellConfigHolder> INSTANCE;

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        registerConfigParameterTypes();
        ImmutableMap.Builder<AbstractSpell, SpellConfigHolder> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            // Build defaults
            SpellConfigHolder config = new SpellConfigHolder();
            DefaultConfig raw = spell.getDefaultConfig();
            config.set(IronConfigParameters.SCHOOL, SchoolRegistry.getSchool(raw.schoolResource));
            config.set(IronConfigParameters.MIN_RARITY, raw.minRarity);
            config.set(IronConfigParameters.MAX_LEVEL, raw.maxLevel);
            config.set(IronConfigParameters.ENABLED, raw.enabled);
            config.set(IronConfigParameters.COOLDOWN_IN_SECONDS, raw.cooldownInSeconds);
            config.set(IronConfigParameters.ALLOW_CRAFTING, raw.allowCrafting);
            // Data Driven Overrides
            ResourceLocation spellId = spell.getSpellResource();
            if (data.containsKey(spellId)) {
                try {
                    JsonObject json = data.get(spellId).getAsJsonObject();
                    for (SpellConfigParameter<?> paramType : ALL_TYPES) {
                        Optional<JsonElement> elem = resolveJsonElement(spellId, paramType, json);
                        if (elem.isPresent()) {
                            var decoded = paramType.datatype().decode(registryops, elem.get()).getOrThrow().getFirst();
                            config.set((SpellConfigParameter) paramType, decoded);
                        }
                    }
                } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                    IronsSpellbooks.LOGGER.error("Parsing error loading spell config {}: {}", spellId, jsonparseexception);
                }
            }
            builder.put(spell, config);
        }
        INSTANCE = builder.build();
        // Second pass for events. Allows for data-completion (can reference existing config values), and higher context (avoid overriding user-input)
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            NeoForge.EVENT_BUS.post(new ModifyDefaultConfigValuesEvent(spell, INSTANCE.get(spell)));
        }
    }

    private Optional<JsonElement> resolveJsonElement(ResourceLocation spellId, SpellConfigParameter<?> dataType, JsonObject parent) {
        if (parent.has(dataType.key().toString())) {
            return Optional.of(parent.get(dataType.key().toString()));
        } else if (parent.has(dataType.key().getPath())) {
            if (!dataType.key().getNamespace().equals("irons_spellbooks")) {
                IronsSpellbooks.LOGGER.warn("Config for {} has ambiguous entry \"{}\", adapting to \"{}\"", spellId, dataType.key().getPath(), dataType.key());
            }
            return Optional.of(parent.get(dataType.key().getPath()));
        } else {
            return Optional.empty();
        }
    }

    public static <T> T getSpellConfigValue(AbstractSpell spell, SpellConfigParameter<T> parameterType) {
        if (!INSTANCE.containsKey(spell)) {
            throw new RuntimeException(String.format("Invalid/unregistered spell \"%s\" attempting to lookup config! Crashing!", spell.getClass().getCanonicalName()));
        }
        return INSTANCE.get(spell).get(parameterType).orElse(parameterType.defaultValue());
    }
}
