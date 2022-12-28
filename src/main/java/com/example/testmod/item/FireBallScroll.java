package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.scroll.data.ScrollDataProvider;
import com.example.testmod.capabilities.scroll.network.PacketUseScroll;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

public class FireBallScroll extends AbstractScroll {
    public FireBallScroll(int level, Rarity rarity) {
        super(SpellType.FIREBALL_SPELL, level, rarity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);
        var scrollData = getScrollData(stack);
        scrollData.getSpell().onCast(stack, level, player);

        /*
        TestMod.LOGGER.info("scroll.stack.getItem().getDescription().getString():" + scroll.stack.getItem().getDescription().getString());
        TestMod.LOGGER.info("scroll.stack.getItem().hashCode():" + scroll.stack.getItem().hashCode());
        TestMod.LOGGER.info("scroll.stack.hashCode():" + scroll.stack.hashCode());
        TestMod.LOGGER.info("stack.getItem().getDescription().getString():" + stack.getItem().getDescription().getString());
        TestMod.LOGGER.info("stack.getItem().hashCode():" + stack.getItem().hashCode());
        TestMod.LOGGER.info("stack.hashCode():" + stack.hashCode());
        */

        removeScrollAfterCast(player, stack);

        return InteractionResultHolder.success(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        //The CompoundTag passed in here will be attached to the ItemStack by forge so you can add additional items to it if you need
        return new ScrollDataProvider(spellType, level);
    }
}
