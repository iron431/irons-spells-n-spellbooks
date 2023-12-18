package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelectionManager;
import io.redspace.ironsspellbooks.network.ServerboundQuickCast;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.shadowed.eliotlash.mclib.math.functions.limit.Min;

import java.util.List;

public class CastingItem extends Item {
    public CastingItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        SpellSelectionManager spellSelectionManager = new SpellSelectionManager(player);
        SpellSelectionManager.SpellSlot spellSlot = spellSelectionManager.getSelectedSpellSlot();
        SpellData spellData = spellSlot.spellData;
        if (spellData.equals(SpellData.EMPTY)) {
            //IronsSpellbooks.LOGGER.debug("CastingItem.Use.1 {} {}", level.isClientSide, hand);
            return InteractionResultHolder.pass(itemStack);
        }

        if (level.isClientSide()) {
            if (ClientMagicData.isCasting()) {
                //IronsSpellbooks.LOGGER.debug("CastingItem.Use.2 {} {}", level.isClientSide, hand);
                return InteractionResultHolder.consume(itemStack);
            } else if (ClientMagicData.getPlayerMana() < spellData.getSpell().getManaCost(spellData.getLevel(), player)
                    || ClientMagicData.getCooldowns().isOnCooldown(spellData.getSpell())
                    || !ClientMagicData.getSyncedSpellData(player).isSpellLearned(spellData.getSpell())) {
                //IronsSpellbooks.LOGGER.debug("CastingItem.Use.3 {} {}", level.isClientSide, hand);
                return InteractionResultHolder.pass(itemStack);
            } else {
                //IronsSpellbooks.LOGGER.debug("CastingItem.Use.4 {} {}", level.isClientSide, hand);
                return InteractionResultHolder.consume(itemStack);
            }
        }

        if (spellData.getSpell().attemptInitiateCast(itemStack, spellData.getLevel(), level, player, spellSlot.getCastSource(), true)) {
            if (spellData.getSpell().getCastType().holdToCast()) {
                player.startUsingItem(hand);
            }
            //IronsSpellbooks.LOGGER.debug("CastingItem.Use.5 {} {}", level.isClientSide, hand);

            return InteractionResultHolder.consume(itemStack);
        } else {
            //IronsSpellbooks.LOGGER.debug("CastingItem.Use.6 {} {}", level.isClientSide, hand);
            return InteractionResultHolder.fail(itemStack);
        }
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 7200;//return getSpellBookData(itemStack).getActiveSpell().getCastTime();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int p_41415_) {
        IronsSpellbooks.LOGGER.debug("Spellbook Release Using ticks used: {}", p_41415_);
        entity.stopUsingItem();
        Utils.releaseUsingHelper(entity, itemStack, p_41415_);
        super.releaseUsing(itemStack, p_41413_, entity, p_41415_);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        MinecraftInstanceHelper.ifPlayerPresent((player) -> {
            SpellSelectionManager manager = new SpellSelectionManager(player);
            if (manager.getSelectedSpellSlot() != null) {
                pTooltipComponents.addAll(TooltipsUtils.formatActiveSpellTooltip(pStack, manager.getSelectedSpellData(), manager.getSelectedSpellSlot().getCastSource(), (LocalPlayer) player));
            }
        });
    }
}
