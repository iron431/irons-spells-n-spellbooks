package io.redspace.ironsspellbooks.item;


import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.ISkillContainerMutable;
import io.redspace.skillcasting.data.SkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.data.SkillSlot;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Scroll extends Item {

    public Scroll(Item.Properties properties) {
        super(properties);
    }

    public static @Nullable SkillData getSpellSlotFromStack(ItemStack itemStack) {
        if (!ISkillContainer.isSkillContainer(itemStack)) {
            return null;
        }
        var container = ISkillContainer.get(itemStack);
        if (container.isEmpty()) {
            return null;
        }
        return container.getSkillAtIndex(0);
    }

    public static ISkillContainer createScrollContainer(SkillData skillData) {
        return new SkillContainer(1, false, false, new SkillSlot[]{new SkillSlot(skillData, 0)});
    }

    public static void applyScrollToStack(ItemStack stack, AbstractSpellSkill spell, int level) {
        ISkillContainer.set(stack, createScrollContainer(new SkillData(spell, level)));
    }

    public static void applyImbuedToStack(ItemStack stack, AbstractSpellSkill spell, int level) {
        ISkillContainer.set(stack, SkillContainer.create(false, new SkillData(spell, level, true)));
    }

    public static ISkillContainerMutable getOrCreateContainer(ItemStack stack, int maxSlots, boolean spellWheel, boolean mustEquip) {
        if (!ISkillContainer.isSkillContainer(stack)) {
            ISkillContainer.set(stack, new SkillContainer(maxSlots, spellWheel, mustEquip));
        }
        return ISkillContainer.get(stack).mutableCopy();
    }

    protected void removeScrollAfterCast(ServerPlayer serverPlayer, ItemStack stack) {
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
    }

    public static void attemptRemoveScrollAfterCast(ServerPlayer serverPlayer) {
        // fixme: cast item tracking
//        ItemStack potentialScroll = MagicData.get(serverPlayer).getPlayerCastingItem();
//        if (potentialScroll.getItem() instanceof Scroll scroll) {
//            scroll.removeScrollAfterCast(serverPlayer, potentialScroll);
//        }
    }

    @Override
    public @Nullable String getCreatorModId(ItemStack itemStack) {
        var spell = getSpellSlotFromStack(itemStack).getSkill();
        var id = SkillcastingRegistries.SKILL_REGISTRY.getKey(spell);
        return id == null ? super.getCreatorModId(itemStack) : id.getNamespace();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var spellSlot = getSpellSlotFromStack(stack);
        // todo: is there a point of limiting scrolls to only spells?
        if (spellSlot == null /*|| !(spellSlot.getSkill() instanceof AbstractSpellSkill spell)*/) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        boolean cast = SkillcastingManager.attemptInitiateCast(
                CasterRef.entity(player),
                spellSlot.getHolder(),
                spellSlot.getLevel(),
                CastSource.SCROLL.name());
        if (cast) {
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        // fixme: create "%s Scroll" lang entry
        return getSpellSlotFromStack(itemStack).getSkill().getDisplayName(null);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, lines, flag);
        MinecraftInstanceHelper.ifPlayerPresent(player -> {
            lines.addAll(TooltipsUtils.formatScrollTooltip(itemStack, player));
        });
    }
}
