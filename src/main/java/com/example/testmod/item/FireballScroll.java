package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.scroll.data.ScrollDataProvider;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class FireballScroll extends AbstractScroll {
    public FireballScroll(int level, Rarity rarity) {
        super(SpellType.FIREBALL_SPELL, level, rarity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        TestMod.LOGGER.info("FireBallScroll.use: level.isClientSide:" + level.isClientSide);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        } else {
            ItemStack stack = player.getItemInHand(hand);

            if (stack.getItem() instanceof FireballScroll scroll) {
                var scrollData = scroll.getScrollData();
                scrollData.getSpell().onCast(stack, level, player);
            }
        }
        //Messages.sendToServer(new PacketUseScroll(this.spellType, this.level));
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        //The CompoundTag passed in here will be attached to the ItemStack by forge so you can add additional items to it if you need
        this.stack = stack;
        return new ScrollDataProvider(spellType, level, stack, nbt);
    }
}
