package io.redspace.ironsspellbooks.api.item.curios;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public record AffinityData(Map<ResourceLocation, Integer> affinityData) {
    public static final String NBT = "irons_spellbooks:affinity_data";
    public static final String LEGACY_NBT = "ISBEnhance";

    public static final Codec<AffinityData> MULTI_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("bonuses").forGetter(AffinityData::affinityData)
    ).apply(builder, AffinityData::new));

    public static final Codec<AffinityData> CODEC = MULTI_CODEC;//CodecHelper.withAlternative(MULTI_CODEC, SINGLE_CODEC);

    public static final Codec<AffinityData> LEGACY_CODEC = CodecHelper.createLegacyCodec((tag) -> {
        String spellId = ((StringTag) tag).getAsString();
        return new AffinityData(spellId);
    });
//    public static final StreamCodec<ByteBuf, AffinityData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static final AffinityData NONE = new AffinityData(Map.of());

    private AffinityData(String id) {
        this(Map.of(ResourceLocation.parse(id), 1));
    }

    public AffinityData(AbstractSpell spell) {
        this(spell.getSpellId());
    }

    public static AffinityData getAffinityData(ItemStack stack) {
        return CodecHelper.getOrElseWithLegacy(stack, NBT, CODEC, NONE, LEGACY_NBT, LEGACY_CODEC);
    }

    public static void setAffinityData(ItemStack stack, AbstractSpell spell) {
        set(stack, new AffinityData(spell));
    }

    public static void setAffinityData(ItemStack stack, AbstractSpell spell, int bonus) {
        set(stack, new AffinityData(Map.of(spell.getSpellResource(), bonus)));
    }

    public static void set(ItemStack stack, AffinityData data) {
        CodecHelper.set(stack, NBT, CODEC, data);
    }

    public static boolean hasAffinityData(ItemStack itemStack) {
        return CodecHelper.hasWithLegacy(itemStack, NBT, LEGACY_NBT);
    }

    @Deprecated(forRemoval = true)
    public AbstractSpell getSpell() {
        return affinityData.keySet().stream().findFirst().map(SpellRegistry::getSpell).orElse(SpellRegistry.none());
    }

    public int getBonusFor(AbstractSpell spell) {
        return affinityData.getOrDefault(spell.getSpellResource(), 0);
    }

    public boolean hasBonusFor(AbstractSpell spell) {
        return getBonusFor(spell) != 0;
    }

    public String getNameForItem() {
        return getSpell() == SpellRegistry.none() ? Component.translatable("tooltip.irons_spellbooks.no_affinity").getString() : getSpell().getSchoolType().getDisplayName().getString();
    }

    public List<MutableComponent> getDescriptionComponent() {
        HashMultimap<Integer, AbstractSpell> byLevel = HashMultimap.create();
        affinityData.forEach((key, value) -> byLevel.put(value, SpellRegistry.getSpell(key)));
        return byLevel.keySet().stream().map(key ->
        {
            MutableComponent spellListComponent = Component.literal("").withStyle(ChatFormatting.YELLOW);
            var spells = byLevel.get(key).stream().toList();
            for (int i = 0; i < spells.size(); i++) {
                var spell = spells.get(i);
                spellListComponent.append(Component.translatable(spell.getComponentId()).withStyle(spell.getSchoolType().getDisplayName().getStyle()));
                if (i != spells.size() - 1) {
                    spellListComponent.append(", ");
                }
            }
            return key == 1 ? Component.translatable("tooltip.irons_spellbooks.enhance_spell_level", spellListComponent).withStyle(ChatFormatting.YELLOW) : Component.translatable("tooltip.irons_spellbooks.enhance_spell_level_plural", key, spellListComponent).withStyle(ChatFormatting.YELLOW);
        }).toList();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof AffinityData affinityData && affinityData.affinityData.equals(this.affinityData));
    }

    @Override
    public int hashCode() {
        return this.affinityData.hashCode();
    }
}
