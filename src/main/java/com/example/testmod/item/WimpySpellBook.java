package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataProvider;
import com.example.testmod.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class WimpySpellBook extends AbstractSpellBook {

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        var spellBookData = getSpellBookData(itemStack);
        AbstractSpell spell = spellBookData.getActiveSpell();

        if (level.isClientSide) {
            TestMod.LOGGER.info("WimpySpellBook.use: client");
            if (spell != null
                    && ClientMagicData.getPlayerMana() > spell.getManaCost()
                    && !ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType())
            ) {
                ClientMagicData.getCooldowns().addCooldown(spell.getSpellType(), spell.getSpellCooldown());
                TestMod.LOGGER.info("WimpySpellBook.use: success");
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
            TestMod.LOGGER.info("WimpySpellBook.use: consume");
            //player.stopUsingItem();
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }

        TestMod.LOGGER.info("WimpySpellBook.use: server");

        if (spell != null && spell.attemptCast(itemStack, level, player)) {
            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.fail(itemStack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        //The CompoundTag passed in here will be attached to the ItemStack by forge so you can add additional items to it if you need
        return new SpellBookDataProvider(2, stack, nbt);
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
