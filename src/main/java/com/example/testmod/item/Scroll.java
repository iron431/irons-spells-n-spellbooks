package com.example.testmod.item;

import com.example.testmod.capabilities.scroll.data.ScrollData;
import com.example.testmod.capabilities.scroll.data.ScrollDataProvider;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class Scroll extends Item {

    protected SpellType spellType;
    protected int level;

    public Scroll() {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
    }

    public void setSpellType(SpellType spellType) {
        this.spellType = spellType;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    protected void removeScrollAfterCast(Player player, ItemStack stack) {
        if (!player.isCreative())
            player.getInventory().removeItem(stack);
    }

    public ScrollData getScrollData(ItemStack stack) {
        return stack.getCapability(ScrollDataProvider.SCROLL_DATA).resolve().get();
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
        //nbt in this method is what is persisted on disk
        return new ScrollDataProvider(nbt);
    }
}
