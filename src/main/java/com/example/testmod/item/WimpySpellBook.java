package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.mana.data.PlayerMana;
import com.example.testmod.capabilities.mana.data.PlayerManaProvider;
import com.example.testmod.capabilities.spellbook.data.SpellBookData;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class WimpySpellBook extends Item {
    public WimpySpellBook() {

        //MilkBucketItem
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack item = player.getItemInHand(hand);

        if (item.getItem() instanceof WimpySpellBook) {
            TestMod.LOGGER.info("WimpySpellBook in hand");

            var spellSlots = item.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).map(SpellBookData::getSpellSlots);
            TestMod.LOGGER.info("WimpySpellBook has" + spellSlots + "spell slots");
        } else {
            TestMod.LOGGER.info("WimpySpellBook not found in hand");
        }

        return InteractionResultHolder.pass(item);
    }

}
