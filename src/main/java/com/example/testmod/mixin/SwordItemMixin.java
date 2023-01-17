package com.example.testmod.mixin;

import com.example.testmod.TestMod;
import com.example.testmod.network.PacketCancelCast;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import javax.imageio.ImageTranscoder;
import java.util.List;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends Item {
    public SwordItemMixin(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack swordStack = player.getItemInHand(hand);
        var spell = Utils.getScrollData(swordStack).getSpell();
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            if (level.isClientSide) {
                spell.onClientPreCast(level, player, hand, null);
                if (ClientMagicData.isCasting) {
                    Messages.sendToServer(new PacketCancelCast(false));
                }
                if (spell.getCastType() == CastType.CONTINUOUS) {
                    player.startUsingItem(hand);
                }
                return InteractionResultHolder.success(swordStack);
            }

            if (spell.attemptInitiateCast(swordStack, level, player, false, true)) {
                return InteractionResultHolder.success(swordStack);
            } else {
                return InteractionResultHolder.fail(swordStack);
            }
        }
        return super.use(level, player, hand);
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
