package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.scroll.data.ScrollData;
import com.example.testmod.capabilities.scroll.data.ScrollDataProvider;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Scroll extends Item {

    protected SpellType spellType;
    protected int level = 0;

    public Scroll() {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        setSpellType(SpellType.NONE);
    }

    public Scroll(SpellType spellType, int level) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        setSpellType(spellType);
        this.level = level;
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
        scrollData.getSpell().onCast(level, player);

        removeScrollAfterCast(player, stack);

        return InteractionResultHolder.success(stack);
    }
    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        var scrollData = getScrollData(itemStack);
        return scrollData.getDisplayName();

    }
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.addAll(getScrollData(itemStack).getHoverText());
        super.appendHoverText(itemStack, level, lines, flag);
    }
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            var scrollDataProvider = new ScrollDataProvider(nbt);
            this.spellType = scrollDataProvider.getSpellType();
            this.level = scrollDataProvider.getLevel();
            return scrollDataProvider;
        } else {
            return new ScrollDataProvider(this.spellType, this.level);
        }
    }
}
