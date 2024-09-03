package io.redspace.ironsspellbooks.item;


import io.redspace.ironsspellbooks.api.item.IScroll;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Scroll extends Item implements IScroll {

    public Scroll() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    private AbstractSpell getSpellFromStack(ItemStack itemStack) {
        return ISpellContainer.get(itemStack).getSpellAtIndex(0).getSpell();
    }

    private SpellData getSpellSlotFromStack(ItemStack itemStack) {
        return ISpellContainer.get(itemStack).getSpellAtIndex(0);
    }

    protected void removeScrollAfterCast(ServerPlayer serverPlayer, ItemStack stack) {
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
    }

    public static void attemptRemoveScrollAfterCast(ServerPlayer serverPlayer) {
        ItemStack potentialScroll = MagicData.getMagicData(serverPlayer).getPlayerCastingItem();
        if (potentialScroll.getItem() instanceof Scroll scroll) {
            scroll.removeScrollAfterCast(serverPlayer, potentialScroll);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var spellSlot = getSpellSlotFromStack(stack);
        var spell = spellSlot.getSpell();

        if (level.isClientSide) {
            if (ClientMagicData.isCasting()) {
                return InteractionResultHolder.consume(stack);
            } else if (!ClientMagicData.getSyncedSpellData(player).isSpellLearned(spell)) {
                return InteractionResultHolder.pass(stack);
            } else {
                return InteractionResultHolder.consume(stack);
            }
        }

        var castingSlot = hand.ordinal() == 0 ? SpellSelectionManager.MAINHAND : SpellSelectionManager.OFFHAND;

        if (spell.attemptInitiateCast(stack, spell.getLevelFor(spellSlot.getLevel(), player), level, player, CastSource.SCROLL, false, castingSlot)) {
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        return getSpellSlotFromStack(itemStack).getDisplayName();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, lines, flag);
        MinecraftInstanceHelper.ifPlayerPresent(player -> {
            lines.addAll(TooltipsUtils.formatScrollTooltip(itemStack, player));
        });
    }
}
