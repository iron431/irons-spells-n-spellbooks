package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.tetra.TetraProxy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ServerSafeUtils {
    public static boolean canImbue(ItemStack itemStack) {
        if ((itemStack.getItem() instanceof SwordItem swordItem && !(swordItem instanceof UniqueItem))) {
            return true;
        }

        return TetraProxy.PROXY.canImbue(itemStack);
    }

    public static InteractionResultHolder<ItemStack> onUseCastingHelper(@NotNull Level level, Player player, @NotNull InteractionHand hand, ItemStack stack, AbstractSpell spell) {
        //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.1");
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.2");
            if (level.isClientSide) {
                //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.3");
                if (ClientMagicData.isCasting()) {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.4");
                    return InteractionResultHolder.fail(stack);
                } else if (ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType()) || (ServerConfigs.SWORDS_CONSUME_MANA.get() && ClientMagicData.getPlayerMana() < spell.getManaCost())) {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.5");
                    return InteractionResultHolder.pass(stack);
                } else {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.6");
                    spell.onClientPreCast(level, player, hand, null);
                    if (spell.getCastType().holdToCast()) {
                        IronsSpellbooks.LOGGER.debug("onUseCastingHelper.1");
                        player.startUsingItem(hand);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }

            if (spell.attemptInitiateCast(stack, level, player, CastSource.SWORD, true)) {
                if (spell.getCastType().holdToCast()) {
                    IronsSpellbooks.LOGGER.debug("onUseCastingHelper.2");
                    player.startUsingItem(hand);
                }
                return InteractionResultHolder.success(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        }
        return null;
    }
}
