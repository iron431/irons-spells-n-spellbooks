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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Scroll extends Item {

    protected SpellType spellType;
    protected int level = 0;
    protected TranslatableComponent displayName;
    public Scroll() {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        setSpellType(SpellType.NONE);
    }

    public Scroll(SpellType spellType, int level) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        setSpellType(spellType);
        this.level = level;
    }
    @Override
    public Component getName(ItemStack itemStack) {
        if(true)
        return displayName;
        var scrollComponent = new TranslatableComponent(this.getDescriptionId(itemStack));
        TestMod.LOGGER.info("this class: \t\t" + this.hashCode());
        TestMod.LOGGER.info("class of stack:\t" + itemStack.getItem().hashCode());
        TestMod.LOGGER.info("hash of registry:\t" + ItemRegistry.SCROLL.get().hashCode());
        TestMod.LOGGER.info("my spell:\t\t\t" + spellType);
        TestMod.LOGGER.info("spell of stack:\t" + ((Scroll)itemStack.getItem()).spellType);
        TestMod.LOGGER.info("hash of stack:\t" + itemStack.hashCode());
        TestMod.LOGGER.info("this level: " + this.level);
        TestMod.LOGGER.info("itemstack level:" + ((Scroll)itemStack.getItem()).level);
        TestMod.LOGGER.info(Utils.GetStackTraceAsString());
        return scrollComponent;
    }
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        if(itemStack.getItem() instanceof Scroll scroll){
            TranslatableComponent spellComponent = SpellType.getDisplayName(scroll.spellType);
            var spellLevel = scroll.level;
            lines.add(spellComponent.append(" "+spellLevel).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }
        super.appendHoverText(itemStack, level, lines, flag);
    }
    public void setSpellType(SpellType spellType) {
        displayName = new TranslatableComponent(""+this.level);

        this.spellType = spellType;
    }

    @Override
    public Component getHighlightTip(ItemStack item, Component displayName) {
        return super.getHighlightTip(item, displayName);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    protected void removeScrollAfterCast(Player player, ItemStack stack) {
        if (!player.isCreative())
            player.getInventory().removeItem(stack);
    }

    public ScrollData getScrollData(ItemStack stack) {
        TestMod.LOGGER.info("Scroll.getScrollData(ItemStack stack)");
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
