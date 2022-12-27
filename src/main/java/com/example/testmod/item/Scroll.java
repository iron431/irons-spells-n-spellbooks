package com.example.testmod.item;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class Scroll extends Item {

    public Scroll() {
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));

    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ListTag tag = new ListTag();
        CompoundTag element = new CompoundTag();
        element.putString("id", SpellType.FIREBALL_SPELL.getId());
        element.putShort("lvl", (short)2 );
        tag.add(element);
        stack.getOrCreateTag().put("StoredSpells",tag);

        return InteractionResultHolder.success(stack);
    }
}
