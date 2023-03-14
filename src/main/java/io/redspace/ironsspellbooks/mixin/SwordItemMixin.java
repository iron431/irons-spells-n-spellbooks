package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.scroll.ScrollDataProvider;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends Item {

    public SwordItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var spell = Utils.getScrollData(stack).getSpell();
        //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.1");
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.2");
            if (level.isClientSide) {
                //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.3");
                if (ClientMagicData.isCasting()) {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.4");
                    return InteractionResultHolder.fail(stack);
                } else if (ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType()) || (ServerConfigs.SWORDS_CONSUME_MANA.get() && ClientMagicData.getPlayerMana() < spell.getManaCost())) {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.5");
                    return InteractionResultHolder.pass(stack);
                } else {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.6");
                    spell.onClientPreCast(level, player, hand, null);
                    if (spell.getCastType().holdToCast()) {
                        player.startUsingItem(hand);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }

            if (spell.attemptInitiateCast(stack, level, player, CastSource.SWORD, true)) {
                if (spell.getCastType().holdToCast()) {
                    player.startUsingItem(hand);
                }
                return InteractionResultHolder.success(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        var spell = Utils.getScrollData(itemStack).getSpell();
        if (spell.getSpellType() != SpellType.NONE_SPELL)
            return 7200;//return spell.getCastTime();
        else
            return super.getUseDuration(itemStack);
    }

//    @Override
//    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
//        var spell = Utils.getScrollData(itemStack).getSpell();
//        if (spell.getSpellType() != SpellType.NONE_SPELL)
//            return spell.getSpellType().getUseAnim();
//        else
//            return getUseAnimation(itemStack);
//    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, LivingEntity entity, int ticksUsed) {
        var spell = Utils.getScrollData(itemStack).getSpell();

        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            entity.stopUsingItem();
            Utils.releaseUsingHelper(entity);
        }

        super.releaseUsing(itemStack, level, entity, ticksUsed);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, level, lines, flag);

        var spell = Utils.getScrollData(itemStack).getSpell();
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            lines.add(Component.translatable("tooltip.irons_spellbooks.imbued_tooltip").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal(" ").append(Component.translatable("tooltip.irons_spellbooks.spell_title", spell.getSpellType().getDisplayName(), spell.getLevel()).withStyle(spell.getSpellType().getSchoolType().getDisplayName().getStyle())));
            for (MutableComponent component : spell.getUniqueInfo())
                lines.add(Component.literal(" ").append(component.withStyle(ChatFormatting.DARK_GREEN)));
            if (spell.getCastType() != CastType.INSTANT) {
                String castKey = spell.getCastType() == CastType.CONTINUOUS ? "tooltip.irons_spellbooks.cast_continuous" : "tooltip.irons_spellbooks.cast_long";
                lines.add(Component.literal(" ").append(Component.translatable(castKey, Utils.timeFromTicks(spell.getCastTime(), 1)).withStyle(ChatFormatting.BLUE)));
            }
            lines.add(Component.translatable("tooltip.irons_spellbooks.mana_cost", spell.getManaCost()).withStyle(ChatFormatting.BLUE));
            lines.add(Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(spell.getSpellCooldown() * 2, 1)).withStyle(ChatFormatting.BLUE));
        }

    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag shareTag = new CompoundTag();
        CompoundTag tag = stack.getTag();
        //irons_spellbooks.LOGGER.debug("Scroll.getShareTag.1: {}", tag);
        if (tag != null) {
            shareTag.put(Scroll.TAG, tag);
        }

        Utils.getScrollDataProvider(stack).ifPresent(
                (scrollDataProvider) -> {
                    var newNbt = scrollDataProvider.saveNBTData();
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.getShareTag.2: {}", newNbt);
                    shareTag.put(Scroll.CAP, newNbt);
                }
        );

        return shareTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            //irons_spellbooks.LOGGER.debug("SwordItemMixin.readShareTag.1: {}", nbt);
            stack.setTag(nbt.contains(Scroll.TAG) ? nbt.getCompound(Scroll.TAG) : null);
            if (nbt.contains(Scroll.CAP)) {
                //irons_spellbooks.LOGGER.debug("SwordItemMixin.readShareTag.2");
                Utils.getScrollData(stack).loadNBTData(nbt.getCompound(Scroll.CAP));
            }
        } else {
            //irons_spellbooks.LOGGER.debug("SwordItemMixin.readShareTag.3");
            stack.setTag(null);
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        var scrollDataProvider = new ScrollDataProvider();

        if (nbt != null) {
            //irons_spellbooks.LOGGER.debug("SwordItemMixin.initCapabilities.1: {}", nbt);
            scrollDataProvider.deserializeNBT(nbt.getCompound(Scroll.PARENT));
        } else {
            //irons_spellbooks.LOGGER.debug("SwordItemMixin.initCapabilities.2");
            scrollDataProvider.getOrCreateScrollData();
        }
        return scrollDataProvider;
    }
}
