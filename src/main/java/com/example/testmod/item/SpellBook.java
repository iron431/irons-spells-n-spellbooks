package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.data.SpellBookData;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataProvider;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.player.ClientPlayerEvents;
import com.example.testmod.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
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

public class SpellBook extends Item {

    public SpellBook() {
        this(1, Rarity.UNCOMMON);
    }

    public SpellBook(int spellSlots, Rarity rarity) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(rarity));
    }

    public SpellBookData getSpellBookData(ItemStack stack) {
        return stack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).resolve().get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        var spellBookData = getSpellBookData(itemStack);
        AbstractSpell spell = spellBookData.getActiveSpell();

        if (level.isClientSide()) {

            TestMod.LOGGER.info("CLIENT: WimpySpellBook.use:");
            if (spell != null
                    && ClientMagicData.getPlayerMana() > spell.getManaCost()
                    && !ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType())
            ) {
                TestMod.LOGGER.info("CLIENT: WimpySpellBook.use: sidedSuccess");
                return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
            }

            TestMod.LOGGER.info("CLIENT: WimpySpellBook.use: fail");
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        TestMod.LOGGER.info("SERVER: WimpySpellBook.use: " + level.getServer().getTickCount() + " " + level.getServer().getAverageTickTime());
        if (spell != null && spell.attemptCast(itemStack, level, player)) {
            TestMod.LOGGER.info("SERVER: WimpySpellBook.use: sidedSuccess");
            TestMod.LOGGER.info("\n\n\n\n");
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        TestMod.LOGGER.info("SERVER: WimpySpellBook.use: fail");
        TestMod.LOGGER.info("\n\n\n\n");
        return InteractionResultHolder.fail(itemStack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        //The CompoundTag passed in here will be attached to the ItemStack by forge so you can add additional items to it if you need
        return new SpellBookDataProvider(2, stack, nbt);
    }

}
