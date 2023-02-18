package com.example.testmod.item;

import com.example.testmod.capabilities.spellbook.SpellBookData;
import com.example.testmod.capabilities.spellbook.SpellBookDataProvider;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.SpellRarity;
import com.example.testmod.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

    protected static final String PARENT = "Parent";
    public static final String TAG = "tag";
    public static final String CAP = "cap";
    protected final SpellRarity rarity;
    protected final int spellSlots;

    public SpellBook() {
        this(1, SpellRarity.COMMON);
    }

    public SpellBook(int spellSlots, SpellRarity rarity) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        this.spellSlots = spellSlots;
        this.rarity = rarity;
    }

    public SpellRarity getRarity() {
        return rarity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack itemStack = player.getItemInHand(hand);
        var spellBookData = getSpellBookData(itemStack);
        AbstractSpell spell = spellBookData.getActiveSpell();

        if (level.isClientSide()) {
            if (ClientMagicData.isCasting()) {
                return InteractionResultHolder.fail(itemStack);
            } else if (ClientMagicData.getPlayerMana() < spell.getManaCost()
                    || ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType())) {
                return InteractionResultHolder.pass(itemStack);
            } else {
                spell.onClientPreCast(level, player, hand, null);
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
        }

        if (spell.attemptInitiateCast(itemStack, level, player, CastSource.SPELLBOOK, true)) {
            if (spell.getCastType().holdToCast()) {
                player.startUsingItem(hand);
            }
            return InteractionResultHolder.success(itemStack);
        } else {
            return InteractionResultHolder.fail(itemStack);
        }
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 7200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return getSpellBookData(itemStack).getActiveSpell().getSpellType().getUseAnim();
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int p_41415_) {
        entity.stopUsingItem();
        Utils.releaseUsingHelper(entity);
        super.releaseUsing(itemStack, p_41413_, entity, p_41415_);
    }

    public boolean isUnique() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        if (!this.isUnique()) {
            lines.add(Component.translatable("tooltip.testmod.spellbook_rarity", this.rarity.getDisplayName()).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("tooltip.testmod.spellbook_rarity", Component.translatable("tooltip.testmod.spellbook_unique").withStyle(Style.EMPTY.withColor(0xe04324))).withStyle(ChatFormatting.GRAY));
        }


        var selectedSpellText = getSpellBookData(itemStack).getHoverText();
        if (selectedSpellText.size() > 0) {
            lines.add(Component.empty());
            lines.addAll(selectedSpellText);
        }

        super.appendHoverText(itemStack, level, lines, flag);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag shareTag = new CompoundTag();
        CompoundTag tag = stack.getTag();
        //TestMod.LOGGER.debug("SpellBook.getShareTag.1: {}, {}", spellSlots, tag);
        if (tag != null) {
            shareTag.put(TAG, tag);
        }

        getSpellBookDataProvider(stack).ifPresent(
                (spellBookData) -> {
                    var newNbt = spellBookData.saveNBTData();
                    //TestMod.LOGGER.debug("SpellBook.getShareTag.2: {}, {}", spellSlots, newNbt);
                    shareTag.put(CAP, newNbt);
                }
        );

        return shareTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            //TestMod.LOGGER.debug("SpellBook.readShareTag.1: {}, {}", spellSlots, nbt);
            stack.setTag(nbt.contains(TAG) ? nbt.getCompound(TAG) : null);
            if (nbt.contains(CAP)) {
                getSpellBookData(stack).loadNBTData(nbt.getCompound(CAP));
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
            spellBookDataProvider.getOrCreateSpellBookData(spellSlots);
        }
        return spellBookDataProvider;
    }

}
