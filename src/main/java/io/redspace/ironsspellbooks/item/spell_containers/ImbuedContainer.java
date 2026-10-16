package io.redspace.ironsspellbooks.item.spell_containers;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.skillcasting.api.event.SkillSelectionPriority;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import io.redspace.skillcasting.data.skill.SkillContainer;
import io.redspace.skillcasting.data.skill.SkillData;
import io.redspace.skillcasting.data.skill.SkillSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ImbuedContainer extends SkillContainer {
    public static final Codec<ImbuedContainer> CODEC = ISkillContainer.codec(ImbuedContainer::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ImbuedContainer> STREAM_CODEC = ISkillContainer.streamCodec(ImbuedContainer::new);

    public ImbuedContainer(int maxSkills, boolean skillWheel, boolean mustEquip, List<SkillSlot> skills) {
        super(maxSkills, skillWheel, mustEquip, skills, ImbuedContainer::new);
    }

    @Override
    public SkillSelectionPriority getSelectionPriority() {
        return SkillSelectionPriority.ARMOR;
    }

    @Override
    public Mutable<ImbuedContainer> mutableCopy() {
        return new Mutable<>(this);
    }

    /**
     * Builds an imbued container with the given skills and {@code extraSlots} empty slots. Always a skill-wheel container.
     */
    public static ImbuedContainer create(boolean mustEquip, int extraSlots, SkillData... skills) {
        var container = new ImbuedContainer(extraSlots + skills.length, true, mustEquip, List.of()).mutableCopy();
        for (SkillData data : skills) {
            container.addSpell(data);
        }
        return container.toImmutable();
    }

    public static ImbuedContainer create(boolean mustEquip, SkillData... skills) {
        return create(mustEquip, 0, skills);
    }

    public static boolean has(ItemStack stack) {
        Utils.resolveLegacySpellContainer(stack);
        return stack.has(ComponentRegistry.IMBUED_SPELL_CONTAINER);
    }

    public static ImbuedContainer get(ItemStack stack) {
        Utils.resolveLegacySpellContainer(stack);
        return stack.get(ComponentRegistry.IMBUED_SPELL_CONTAINER);
    }

    public static void set(ItemStack stack, ImbuedContainer container) {
        stack.set(ComponentRegistry.IMBUED_SPELL_CONTAINER, container);
    }

    public static ImbuedContainer create(int maxSize, boolean mustEquip) {
        return ImbuedContainer.create(mustEquip, maxSize);
    }

    public static void applyImbue(ItemStack stack, AbstractSpell spell, int level) {
        set(stack, ImbuedContainer.create(false, new SkillData(spell, level)));
    }
}
