package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataProvider;
import com.example.testmod.capabilities.spellbook.data.SpellBookTypes;
import com.example.testmod.spells.AbstractSpell;
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

public class Scroll extends AbstractScroll {

    public Scroll(Rarity rarity) {

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack itemStack = player.getItemInHand(hand);
        var spellData = itemStack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).resolve().get();

        //TODO: remove this code once scrolls can be transcribed into a spellbook
        AbstractSpell s = spellData.getActiveSpell();
        if (s == null) {
            TestMod.LOGGER.info("Adding fireball");
            spellData.addSpell(AbstractSpell.getSpell(SpellType.FIREBALL_SPELL, 1));
        }

        if (spellData.getActiveSpell().attemptCast(itemStack, level, player)) {
            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.fail(itemStack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        //The CompoundTag passed in here will be attached to the ItemStack by forge so you can add additional items to it if you need
        return new SpellBookDataProvider(SpellBookTypes.WimpySpellBook, 2, stack, nbt);
    }
}
