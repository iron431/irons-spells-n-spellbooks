package io.redspace.ironsspellbooks.api.spells;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.config.SpellConfigParameters;
import io.redspace.skillcastingapi.data.AbstractSkill;
import io.redspace.skillcastingapi.registry.SkillRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class SpellConfigManager extends SimpleJsonResourceReloadListener {
    public SpellConfigManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "spell_config");
    }

    public static final Codec<SpellConfigParameters> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.optionalFieldOf("enabled").forGetter(SpellConfigParameters::enabled),
            Codec.BOOL.optionalFieldOf("canBeCrafted").forGetter(SpellConfigParameters::canBeCrafted),
            Codec.BOOL.optionalFieldOf("canBeLooted").forGetter(SpellConfigParameters::canBeLooted),
            Codec.INT.optionalFieldOf("maxLevel").forGetter(SpellConfigParameters::maxLevel),
            SpellRarity.CODEC.optionalFieldOf("minimumRarity").forGetter(SpellConfigParameters::minRarity),
            Codec.DOUBLE.optionalFieldOf("powerMultiplier").forGetter(SpellConfigParameters::powerMultiplier),
            Codec.DOUBLE.optionalFieldOf("manaCostMultiplier").forGetter(SpellConfigParameters::manaCostMultiplier),
            Codec.DOUBLE.optionalFieldOf("cooldownSeconds").forGetter(SpellConfigParameters::cooldownSeconds),
            RegistryFixedCodec.create(SchoolRegistry.SCHOOL_REGISTRY_KEY).optionalFieldOf("school").forGetter(SpellConfigParameters::school)
    ).apply(builder, SpellConfigParameters::new));

    private static ImmutableMap<AbstractSkill, SpellConfigParameters> INSTANCE;

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<AbstractSkill, SpellConfigParameters> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_")) {
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            }
            var registry = SkillRegistry.REGISTRY;
            if (!registry.containsKey(resourcelocation) || !(registry.get(resourcelocation) instanceof AbstractSpellSkill)) {
                IronsSpellbooks.LOGGER.warn("Attempting to read config for invalid spell \"{}\", skipping", resourcelocation);
            }
            try {
                var decoded = CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                builder.put(registry.get(resourcelocation), decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                IronsSpellbooks.LOGGER.error("Parsing error loading spell config {}: {}", resourcelocation, jsonparseexception);
            }
        }

        INSTANCE = builder.build();
    }

    //todo: implement into spells
    public static boolean getEnabled(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).enabled().orElse(spellSkill.getDefaultConfig().enabled);
    }
    //todo: implement into spells

    public static boolean getCanBeCrafted(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).canBeCrafted().orElse(spellSkill.getDefaultConfig().allowCrafting);
    }
    //todo: implement into spells

    public static boolean getCanBeLooted(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).canBeLooted().orElse(spellSkill.getDefaultConfig().allowLooting);
    }
    //todo: implement into spells

    public static int getMaxLevel(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).maxLevel().orElse(spellSkill.getDefaultConfig().maxLevel);
    }
    //todo: implement into spells

    public static SpellRarity getMinRarity(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).minRarity().orElse(spellSkill.getDefaultConfig().minRarity);
    }
    //todo: implement into spells

    public static double getPowerMultiplier(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).powerMultiplier().orElse(1.0);
    }
    //todo: implement into spells

    public static double getManaCostMultiplier(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).manaCostMultiplier().orElse(1.0);
    }

    //todo: implement into spells
    public static int getCooldownTicks(AbstractSpellSkill spellSkill) {
        return (int) (INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).cooldownSeconds().orElse(spellSkill.getDefaultConfig().cooldownInSeconds) * 20);
    }

    //todo: implement into spells
    public static SchoolType getSchool(AbstractSpellSkill spellSkill) {
        return INSTANCE.getOrDefault(spellSkill, SpellConfigParameters.EMPTY).school().map(Holder::value).orElse(SchoolRegistry.getSchool(spellSkill.getDefaultConfig().schoolResource));
    }
}
