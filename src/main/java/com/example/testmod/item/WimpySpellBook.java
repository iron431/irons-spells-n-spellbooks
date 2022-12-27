package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.data.SpellBookData;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataProvider;
import com.example.testmod.capabilities.spellbook.data.SpellBookTypes;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.google.errorprone.annotations.Var;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class WimpySpellBook extends Item {
    public WimpySpellBook() {
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack itemStack = player.getItemInHand(hand);
        var spellData = itemStack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).resolve().get();

        AbstractSpell s = spellData.getActiveSpell();
        if (s == null) {
            spellData.addSpell(AbstractSpell.getSpell(SpellType.FIREBALL_SPELL, 1));
        }

        if (spellData.getActiveSpell().attemptCast(itemStack, level, player))
            return InteractionResultHolder.success(itemStack);

        return InteractionResultHolder.fail(itemStack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SpellBookDataProvider(SpellBookTypes.WimpySpellBook, 2, stack, nbt);
    }

//    @Override
//    public void inventoryTick(ItemStack itemStack, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
//        var spellData = itemStack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).resolve().get();
//        var s = spellData.getActiveSpell();
//        if (s != null)
//            s.tick();
//        super.inventoryTick(itemStack, p_41405_, p_41406_, p_41407_, p_41408_);
//    }

}
