package io.redspace.ironsspellbooks.api.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellConfigManager extends SimpleJsonResourceReloadListener {
    private static final List<SpellConfigParameter<?>> ALL_TYPES = new ArrayList<>();

    public SpellConfigManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_spellbooks/spell_config");
    }

    private static ImmutableMap<AbstractSpell, SpellConfigHolder> INSTANCE;

    public static void registerConfigParameterTypes() {
        ALL_TYPES.clear();
        ALL_TYPES.add(IronConfigParameters.SCHOOL);
        ALL_TYPES.add(IronConfigParameters.MIN_RARITY);
        ALL_TYPES.add(IronConfigParameters.MAX_LEVEL);
        ALL_TYPES.add(IronConfigParameters.ENABLED);
        ALL_TYPES.add(IronConfigParameters.COOLDOWN_IN_SECONDS);
        ALL_TYPES.add(IronConfigParameters.ALLOW_CRAFTING);
        ALL_TYPES.add(IronConfigParameters.MANA_MULTIPLIER);
        ALL_TYPES.add(IronConfigParameters.POWER_MULTIPLIER);
        NeoForge.EVENT_BUS.post(new RegisterConfigParametersEvent(ALL_TYPES));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<AbstractSpell, SpellConfigHolder> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            // Build defaults
            SpellConfigHolder config = new SpellConfigHolder();
            DefaultConfig raw = spell.getDefaultConfig();
            config.set(IronConfigParameters.SCHOOL, raw.schoolResource);
            config.set(IronConfigParameters.MIN_RARITY, raw.minRarity);
            config.set(IronConfigParameters.MAX_LEVEL, raw.maxLevel);
            config.set(IronConfigParameters.ENABLED, raw.enabled);
            config.set(IronConfigParameters.COOLDOWN_IN_SECONDS, raw.cooldownInSeconds);
            config.set(IronConfigParameters.ALLOW_CRAFTING, raw.allowCrafting);
            NeoForge.EVENT_BUS.post(new ModifyDefaultConfigValuesEvent(spell, config));
            // Data Driven Overrides
            ResourceLocation spellId = spell.getSpellResource();
            if (data.containsKey(spellId)) {
                try {
                    JsonObject json = data.get(spellId).getAsJsonObject();
                    for (SpellConfigParameter<?> paramType : ALL_TYPES) {
                        String jsonKey = paramType.key().toString();
                        if (json.has(jsonKey)) {
                            var decoded = paramType.datatype().decode(registryops, json.get(jsonKey)).getOrThrow().getFirst();
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
    }

    public static <T> T getSpellConfigValue(AbstractSpell spell, SpellConfigParameter<T> parameterType) {
        if (!INSTANCE.containsKey(spell)) {
            throw new RuntimeException(String.format("Invalid/unregistered spell \"%s\" attempting to lookup config! Crashing!", spell.getClass().getCanonicalName()));
        }
        return INSTANCE.get(spell).get(parameterType).orElse(parameterType.defaultValue());
    }
}
