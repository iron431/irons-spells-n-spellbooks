package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.item.IScroll;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Scroll extends Item implements IScroll {

    public Scroll() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (/*category == SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB ||*/ category == CreativeModeTab.TAB_SEARCH) {
            SpellRegistry.REGISTRY.get().getValues().stream()
                    .filter(AbstractSpell::isEnabled)
                    .forEach(spell -> {
                        int min = category == SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB ? spell.getMaxLevel() : spell.getMinLevel();

                        for (int i = min; i <= spell.getMaxLevel(); i++) {
                            var itemstack = new ItemStack(ItemRegistry.SCROLL.get());
                            SpellData.setSpellData(itemstack, spell, i);
                            items.add(itemstack);
                        }
                    });
        }
    }

    protected void removeScrollAfterCast(ServerPlayer serverPlayer, ItemStack stack) {
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
    }

    public static boolean attemptRemoveScrollAfterCast(ServerPlayer serverPlayer) {
        ItemStack potentialScroll = MagicData.getPlayerMagicData(serverPlayer).getPlayerCastingItem();
        if (potentialScroll.getItem() instanceof Scroll scroll) {
            scroll.removeScrollAfterCast(serverPlayer, potentialScroll);
            return true;
        } else
            return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var spellData = SpellData.getSpellData(stack);
        var spell = spellData.getSpell();

        if (level.isClientSide) {
            if (ClientMagicData.isCasting() || !ClientMagicData.getSyncedSpellData(player).isSpellLearned(spell)) {
                return InteractionResultHolder.fail(stack);
            } else {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        if (spell.attemptInitiateCast(stack, spellData.getLevel(), level, player, CastSource.SCROLL, false)) {
            if (spell.getCastType() == CastType.INSTANT) {
                removeScrollAfterCast((ServerPlayer) player, stack);
            }
            if (spell.getCastType().holdToCast()) {
                player.startUsingItem(hand);
            }
            return InteractionResultHolder.success(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        return 7200;//return getScrollData(itemStack).getSpell().getCastTime();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, LivingEntity entity, int ticksUsed) {
        //entity.stopUsingItem();
        if (SpellData.getSpellData(itemStack).getSpell().getCastType() != CastType.CONTINUOUS || getUseDuration(itemStack) - ticksUsed >= 4) {
            Utils.releaseUsingHelper(entity, itemStack, ticksUsed);
        }
        super.releaseUsing(itemStack, level, entity, ticksUsed);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        var scrollData = SpellData.getSpellData(itemStack);
        return scrollData.getDisplayName();

    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, List<Component> lines, @NotNull TooltipFlag flag) {
        if (MinecraftInstanceHelper.instance.player() instanceof LocalPlayer localPlayer)
            lines.addAll(TooltipsUtils.formatScrollTooltip(itemStack, localPlayer));
        super.appendHoverText(itemStack, level, lines, flag);
    }
}
