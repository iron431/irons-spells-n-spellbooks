package io.redspace.skillcasting.data.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.event.SkillSelectionPriority;
import io.redspace.skillcasting.data.AbstractSkill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ISkillContainer {

    @FunctionalInterface
    interface Factory<C extends ISkillContainer> {
        C create(int size, boolean skillWheel, boolean mustEquip, List<SkillSlot> slots);
    }

    static <C extends ISkillContainer> Codec<C> codec(Factory<C> factory) {
        return RecordCodecBuilder.create(builder -> builder.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("size").forGetter(ISkillContainer::getMaxSkillCount),
                Codec.BOOL.fieldOf("wheel").forGetter(ISkillContainer::isSkillWheel),
                Codec.BOOL.fieldOf("equip").forGetter(ISkillContainer::mustEquip),
                Codec.list(SkillSlot.CODEC).fieldOf("data").forGetter(ISkillContainer::getActiveSkills)
        ).apply(builder, (size, wheel, equip, slots) -> factory.create(size, wheel, equip,
                slots.stream().filter(slot -> slot.index() >= 0 && slot.index() < size).toList())));
    }

    static <C extends ISkillContainer> StreamCodec<RegistryFriendlyByteBuf, C> streamCodec(Factory<C> factory) {
        return StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ISkillContainer::getMaxSkillCount,
                ByteBufCodecs.BOOL, ISkillContainer::isSkillWheel,
                ByteBufCodecs.BOOL, ISkillContainer::mustEquip,
                SkillSlot.SKILL_SLOT.apply(ByteBufCodecs.list()), ISkillContainer::getActiveSkills,
                factory::create);
    }

    int getMaxSkillCount();

    int getActiveSkillCount();

    int getNextAvailableIndex();

    /**
     * @return Whether this container must be equipped as an Armor/Wearable in order to grant skills, or is Handheld
     */
    boolean mustEquip();

    /**
     * @return Whether this container contributes to the Skill Wheel, or is a self-contained casting item
     */
    boolean isSkillWheel();

    default SkillSelectionPriority getSelectionPriority() {
        return SkillSelectionPriority.OTHER;
    }

    /**
     * @return Array base collection of all skill slots, including empty skill slots
     */
    @NotNull SkillSlot[] getAllSkills();

    /**
     * @return Condensed form of only non-empty skill slots
     */
    @NotNull List<SkillSlot> getActiveSkills();

    @Nullable SkillData getSkillAtIndex(int index);

    int getIndexForSkill(AbstractSkill skill);

    boolean isEmpty();

    ISkillContainerMutable mutableCopy();

    static ISkillContainer create(boolean mustEquip, SkillData... skills) {
        return create(mustEquip, 0, skills);
    }

    static ISkillContainer create(boolean mustEquip, int extraSlots, SkillData... skills) {
        var container = SkillContainer.fromSerialized(extraSlots + skills.length, true, mustEquip, List.of()).mutableCopy();
        for (SkillData data : skills) {
            container.addSkill(data);
        }
        return container.toImmutable();
    }
}
