package io.redspace.ironsspellbooks.api.item.curios;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record AffinityData(Map<Holder<AbstractSpell>, Integer> affinityData) {

    private static final Codec<Map<Holder<AbstractSkill>, Integer>> SKILL_BONUSES_CODEC =
            Codec.unboundedMap(SkillcastingRegistries.SKILL_HOLDER_CODEC, Codec.INT);

    public static final Codec<AffinityData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SKILL_BONUSES_CODEC.fieldOf("bonuses").forGetter(AffinityData::toSkillMap)
    ).apply(builder, AffinityData::fromSkillMap));

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Holder<AbstractSkill>, Integer>> SKILL_BONUSES_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, ByteBufCodecs.VAR_INT);

    public static final StreamCodec<RegistryFriendlyByteBuf, AffinityData> STREAM_CODEC = StreamCodec.composite(
            SKILL_BONUSES_STREAM_CODEC, AffinityData::toSkillMap,
            AffinityData::fromSkillMap);

    public static final AffinityData NONE = new AffinityData(Map.of());

    public AffinityData(AbstractSpell skill) {
        this(skill, 1);
    }

    public AffinityData(AbstractSpell skill, int bonus) {
        this(Map.of(spellHolder(skill), bonus));
    }

    public AffinityData(Holder<? extends AbstractSkill> skill, int bonus) {
        this(Map.of(requireSpellSkillHolder(skill), bonus));
    }

    public static AffinityData of(Map<? extends Holder<? extends AbstractSkill>, Integer> bonuses) {
        Map<Holder<AbstractSpell>, Integer> copy = HashMap.newHashMap(bonuses.size());
        bonuses.forEach((holder, bonus) -> copy.put(requireSpellSkillHolder(holder), bonus));
        return new AffinityData(Map.copyOf(copy));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static AffinityData ofHolders(Map<DeferredHolder<AbstractSkill, ? extends AbstractSpell>, Integer> bonuses) {
        HashMap<Holder<AbstractSpell>, Integer> copy = new HashMap<>(bonuses.size());
        bonuses.forEach((holder, i) -> copy.put((Holder<AbstractSpell>) (Holder) holder, i));
        return new AffinityData(copy);
    }

    public static AffinityData getAffinityData(ItemStack stack) {
        return stack.has(ComponentRegistry.AFFINITY_COMPONENT) ? stack.get(ComponentRegistry.AFFINITY_COMPONENT) : AffinityData.NONE;
    }

    public static void setAffinityData(ItemStack stack, AbstractSpell skill) {
        set(stack, new AffinityData(skill));
    }

    public static void setAffinityData(ItemStack stack, AbstractSpell skill, int bonus) {
        set(stack, new AffinityData(skill, bonus));
    }

    public static void set(ItemStack stack, AffinityData data) {
        stack.set(ComponentRegistry.AFFINITY_COMPONENT, data);
    }

    public static boolean hasAffinityData(ItemStack itemStack) {
        return itemStack.has(ComponentRegistry.AFFINITY_COMPONENT);
    }

    public int getBonusFor(AbstractSpell skill) {
        return getBonusFor(spellHolder(skill));
    }

    public int getBonusFor(Holder<? extends AbstractSkill> skill) {
        if (!(skill.value() instanceof AbstractSpell)) {
            return 0;
        }
        return affinityData.getOrDefault(requireSpellSkillHolder(skill), 0);
    }

    public boolean hasBonusFor(AbstractSpell skill) {
        return getBonusFor(skill) != 0;
    }

    public @Nullable AbstractSpell getFirstSpell() {
        return affinityData.keySet().stream().findFirst().map(Holder::value).orElse(null);
    }

    public String getNameForItem() {
        AbstractSpell firstSkill = getFirstSpell();
        return firstSkill == null
                ? Component.translatable("tooltip.irons_spellbooks.no_affinity").getString()
                : firstSkill.getSchoolType().getDisplayName().getString();
    }

    public List<MutableComponent> getDescriptionComponent() {
        HashMultimap<Integer, AbstractSpell> byLevel = HashMultimap.create();
        affinityData.forEach((key, value) -> byLevel.put(value, key.value()));
        return byLevel.keySet().stream().map(key -> {
            MutableComponent spellListComponent = Component.literal("").withStyle(ChatFormatting.YELLOW);
            var spells = byLevel.get(key).stream().toList();
            for (int i = 0; i < spells.size(); i++) {
                var spell = spells.get(i);
                spellListComponent.append(Component.translatable(spell.getDescriptionId()).withStyle(spell.getSchoolType().getDisplayName().getStyle()));
                if (i != spells.size() - 1) {
                    spellListComponent.append(", ");
                }
            }
            return key == 1
                    ? Component.translatable("tooltip.irons_spellbooks.enhance_spell_level", spellListComponent).withStyle(ChatFormatting.YELLOW)
                    : Component.translatable("tooltip.irons_spellbooks.enhance_spell_level_plural", key, spellListComponent).withStyle(ChatFormatting.YELLOW);
        }).toList();
    }

    @SuppressWarnings("unchecked")
    public static Holder<AbstractSpell> spellHolder(AbstractSpell skill) {
        return (Holder<AbstractSpell>) (Holder<?>) SkillRegistry.holder(skill);
    }

    private static AffinityData fromSkillMap(Map<Holder<AbstractSkill>, Integer> bonuses) {
        return of(bonuses);
    }

    @SuppressWarnings("unchecked")
    private static Map<Holder<AbstractSkill>, Integer> toSkillMap(AffinityData data) {
        Map<Holder<AbstractSkill>, Integer> copy = HashMap.newHashMap(data.affinityData().size());
        data.affinityData().forEach((holder, bonus) -> copy.put((Holder<AbstractSkill>) (Holder<?>) holder, bonus));
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static Holder<AbstractSpell> requireSpellSkillHolder(Holder<? extends AbstractSkill> holder) {
        if (!(holder.value() instanceof AbstractSpell)) {
            throw new IllegalArgumentException("Not a spell skill: " + holder.getRegisteredName());
        }
        return (Holder<AbstractSpell>) (Holder<?>) holder;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof AffinityData other && other.affinityData.equals(this.affinityData));
    }

    @Override
    public int hashCode() {
        return this.affinityData.hashCode();
    }
}
