package com.example.testmod.mixin;

import com.example.testmod.network.PacketCancelCast;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
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
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            if (level.isClientSide) {
                if (ClientMagicData.isCasting) {
                    return InteractionResultHolder.fail(stack);
                } else if (ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType())) {
                    return InteractionResultHolder.pass(stack);
                } else {
                    spell.onClientPreCast(level, player, hand, null);

                    if (spell.getCastType() == CastType.CONTINUOUS) {
                        player.startUsingItem(hand);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }

            if (spell.attemptInitiateCast(stack, level, player, CastSource.Sword, false)) {
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
            return 7200;
        else
            return super.getUseDuration(itemStack);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        var spell = Utils.getScrollData(itemStack).getSpell();
        if (spell.getSpellType() != SpellType.NONE_SPELL)
            return UseAnim.BOW;
        else
            return getUseAnimation(itemStack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, LivingEntity entity, int ticksUsed) {
        var spell = Utils.getScrollData(itemStack).getSpell();
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            entity.stopUsingItem();
            Messages.sendToServer(new PacketCancelCast(true));
        }

        super.releaseUsing(itemStack, level, entity, ticksUsed);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, level, lines, flag);

        var spell = Utils.getScrollData(itemStack).getSpell();
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            lines.add(Component.translatable("tooltip.testmod.imbued_tooltip").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal(" ").append(Component.translatable("tooltip.testmod.spell_title", spell.getSpellType().getDisplayName(), spell.getLevel()).withStyle(spell.getSpellType().getSchoolType().getDisplayName().getStyle())));
            if (spell.getUniqueInfo() != null)
                lines.add(Component.literal(" ").append(spell.getUniqueInfo().withStyle(ChatFormatting.DARK_GREEN)));
            lines.add(Component.translatable("tooltip.testmod.mana_cost", spell.getManaCost()).withStyle(ChatFormatting.BLUE));
            lines.add(Component.translatable("tooltip.testmod.cooldown_length_seconds", Utils.timeFromTicks(spell.getSpellCooldown(), 1)).withStyle(ChatFormatting.BLUE));
        }

    }
}
