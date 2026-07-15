package io.redspace.ironsspellbooks.item;


import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellCastSources;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.data.CastSource;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillContainer;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.data.SkillSlot;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
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

    public static void applyScrollToStack(ItemStack stack, AbstractSpell spell, int level) {
        ISkillContainer.set(stack, createScrollContainer(new SkillData(spell, level)));
    }

    public static void removeScrollAfterCast(ServerPlayer serverPlayer, ItemStack stack) {
        if (!serverPlayer.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
    }

    public static void attemptRemoveScrollAfterCast(ServerPlayer serverPlayer, CastContext castContext) {
        castContext.find(SpellcastingComponentTypes.SCROLL_STACK).ifPresent(
                stack -> removeScrollAfterCast(serverPlayer, stack)
        );
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var spellSlot = getSpellSlotFromStack(stack);
        if (spellSlot == null || !(spellSlot.getSkill() instanceof AbstractSpell spell)) {
            return InteractionResultHolder.fail(stack);
        }
        SkillcastingManager.attemptInitiateCast(
                CasterRef.entity(player),
                spellSlot.getHolder(),
                spellSlot.getLevel(),
                CastSource.of(SpellCastSources.SCROLL, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        var data = getSpellSlotFromStack(itemStack);
        if (data == null) {
            return super.getName(itemStack);
        }
        return Component.translatable(getDescriptionId() + ".framed", data.getSkill().getDisplayName(MinecraftInstanceHelper.getPlayer()));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, lines, flag);
        MinecraftInstanceHelper.ifPlayerPresent(player -> {
            lines.addAll(TooltipsUtils.formatScrollTooltip(itemStack, player));
        });
    }
}
