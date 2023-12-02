package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.network.ServerboundQuickCast;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CastingItem extends Item {
    public CastingItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            var spellBook = Utils.getPlayerSpellbookStack(player);
            var spellBookData = SpellBookData.getSpellBookData(spellBook);
            if (Minecraft.getInstance().screen == null && spellBook != null && spellBookData.getSpell(spellBookData.getActiveSpellIndex()) != SpellData.EMPTY) {
                Messages.sendToServer(new ServerboundQuickCast(spellBookData.getActiveSpellIndex()));
                //handle client swing animation
                var spellData = spellBookData.getActiveSpell();
                if (ClientMagicData.isCasting()) {
                    return InteractionResultHolder.fail(itemStack);
                } else if (ClientMagicData.getPlayerMana() < spellData.getSpell().getManaCost(spellData.getLevel(), player)
                        || ClientMagicData.getCooldowns().isOnCooldown(spellData.getSpell())
                        || !ClientMagicData.getSyncedSpellData(player).isSpellLearned(spellData.getSpell())) {
                    return InteractionResultHolder.fail(itemStack);
                } else {
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            } else {
                return InteractionResultHolder.pass(itemStack);
            }

        }
        return InteractionResultHolder.pass(itemStack);
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
}
