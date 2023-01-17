package com.example.testmod.mixin;

import com.example.testmod.TestMod;
import com.example.testmod.network.PacketCancelCast;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

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
}
