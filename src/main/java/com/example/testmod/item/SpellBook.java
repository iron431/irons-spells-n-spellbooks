package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.network.PacketCancelCast;
import com.example.testmod.capabilities.spellbook.data.SpellBookData;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataProvider;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import org.jline.utils.Log;

import java.util.List;

public class SpellBook extends Item {


    public SpellBook() {
        this(1, Rarity.UNCOMMON);
    }

    public SpellBook(int spellSlots, Rarity rarity) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(rarity));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        /*
            pretty sure we can super easily cancel spell if you dont hold down use and still let quick cast not have to be held
            just have to remember if we WERE using, cancel if we stop, but ONLY START using if we right click

         */

        ItemStack itemStack = player.getItemInHand(hand);
        var spellBookData = getSpellBookData(itemStack);
        AbstractSpell spell = spellBookData.getActiveSpell();

        //
        //  Client Side Use Animation
        //
        if (level.isClientSide()) {
            if (spell != null) {
                if (ClientMagicData.isCasting) {
                    Messages.sendToServer(new PacketCancelCast(false));
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                } else if (ClientMagicData.getPlayerMana() > spell.getManaCost() &&
                        !ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType())
                ) {
                    //TestMod.LOGGER.info(spell.getCastType() + "");
                    if (spell.getCastType() == CastType.CONTINUOUS)
                        player.startUsingItem(hand);
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }

            } else {
                return InteractionResultHolder.fail(itemStack);
            }
        }

        //
        //  Attempt to Cast Spell (attemptCast is serverSide only) (currently)
        //
        if (spell != null && spell.attemptInitiateCast(itemStack, level, player, true, true)) {
            return InteractionResultHolder.success(itemStack);
        }


        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 7200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_41452_) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int p_41415_) {
        entity.stopUsingItem();
        Messages.sendToServer(new PacketCancelCast(true));
        super.releaseUsing(itemStack, p_41413_, entity, p_41415_);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.addAll(getSpellBookData(itemStack).getHoverText());
        super.appendHoverText(itemStack, level, lines, flag);
    }

    public SpellBookData getSpellBookData(ItemStack stack) {
        return stack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).resolve().get();
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        //The CompoundTag passed in here will be attached to the ItemStack by forge so you can add additional items to it if you need
        return new SpellBookDataProvider(2, stack, nbt);
    }

}
