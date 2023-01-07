package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.SpellBookData;
import com.example.testmod.capabilities.spellbook.SpellBookDataProvider;
import com.example.testmod.network.PacketCancelCast;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellBook extends Item implements ISpellBook {

    private static final String PARENT = "Parent";
    private final int spellSlots;

    public SpellBook() {
        this(5, Rarity.UNCOMMON);
    }

    public SpellBook(int spellSlots, Rarity rarity) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(rarity));
        this.spellSlots = spellSlots;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        /*
            pretty sure we can super easily cancel spell if you dont hold down use and still let quick cast not have to be held
            just have to remember if we WERE using, cancel if we stop, but ONLY START using if we right click

         */

        ItemStack itemStack = player.getItemInHand(hand);
        var spellBookData = getSpellBookData(itemStack);
        AbstractSpell spell = spellBookData.getActiveSpell();

        //
        //  Client Side Use Animation
        //
        if (level.isClientSide()) {
            if (spell != null) {
                spell.onClientPreCast(level, player, hand);
                if (ClientMagicData.isCasting) {
                    Messages.sendToServer(new PacketCancelCast(false));
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                } else if (ClientMagicData.getPlayerMana() > spell.getManaCost() &&
                        !ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType())
                ) {
                    //TestMod.LOGGER.debug(spell.getCastType() + "");
                    if (spell.getCastType() == CastType.CONTINUOUS){
                        player.startUsingItem(hand);
                        TestMod.LOGGER.debug("SpellBook: Start Using");
                    }
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }

            } else {
                return InteractionResultHolder.fail(itemStack);
            }
        }

        //
        //  Attempt to Cast Spell
        //
        TestMod.LOGGER.debug("SpellBook.Use.attemptInitiateCast");
        if (spell != null && spell.attemptInitiateCast(itemStack, level, player, false, true)) {
            return InteractionResultHolder.success(itemStack);
        }


        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 7200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_41452_) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int p_41415_) {
        entity.stopUsingItem();
        Messages.sendToServer(new PacketCancelCast(true));
        TestMod.LOGGER.debug("SpellBook: Stop Using");
        super.releaseUsing(itemStack, p_41413_, entity, p_41415_);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.addAll(getSpellBookData(itemStack).getHoverText());
        super.appendHoverText(itemStack, level, lines, flag);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag shareTag = new CompoundTag();
        CompoundTag tag = stack.getTag();
        //TestMod.LOGGER.debug("SpellBook.getShareTag.1: {}, {}", spellSlots, tag);
        if (tag != null) {
            shareTag.put("tag", tag);
        }

        getSpellBookDataProvider(stack).ifPresent(
                (spellBookData) -> {
                    var newNbt = spellBookData.saveNBTData();
                    //TestMod.LOGGER.debug("SpellBook.getShareTag.2: {}, {}", spellSlots, newNbt);
                    shareTag.put("cap", newNbt);
                }
        );

        return shareTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            //TestMod.LOGGER.debug("SpellBook.readShareTag.1: {}, {}", spellSlots, nbt);
            stack.setTag(nbt.contains("tag") ? nbt.getCompound("tag") : null);
            if (nbt.contains("cap")) {
                getSpellBookData(stack).loadNBTData(nbt.getCompound("cap"));
            }
        } else {
            //TestMod.LOGGER.debug("SpellBook.readShareTag.2: {}", spellSlots);
            stack.setTag(null);
        }
    }

    public SpellBookData getSpellBookData(ItemStack stack) {
        //TestMod.LOGGER.debug("SpellBook.getSpellBookData.1 {}", stack.hashCode());
        return stack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).resolve().get();
    }

    public LazyOptional<SpellBookData> getSpellBookDataProvider(ItemStack stack) {
        //TestMod.LOGGER.debug("SpellBook.getSpellBookData.2 {}", stack.hashCode());
        return stack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA);
    }


    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        var spellBookDataProvider = new SpellBookDataProvider();

        if (nbt != null) {
            //TestMod.LOGGER.debug("SpellBook.initCapabilities.1: {}, {}", spellSlots, nbt);
            spellBookDataProvider.deserializeNBT(nbt.getCompound(PARENT));
        } else {
            //TestMod.LOGGER.debug("SpellBook.initCapabilities.2: {}", spellSlots);
            spellBookDataProvider.getOrCreateSpellbookData(spellSlots);
        }
        return spellBookDataProvider;
    }

}
