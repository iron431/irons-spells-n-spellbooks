package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundQuickCast {

    private int slot;
    private InteractionHand hand;

    public ServerboundQuickCast(int slot, InteractionHand hand) {
        this.slot = slot;
        this.hand = hand;
    }

    public ServerboundQuickCast(FriendlyByteBuf buf) {
        slot = buf.readInt();
        hand = buf.readEnum(InteractionHand.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeEnum(hand);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            var itemStack = serverPlayer.getItemInHand(hand);
            SpellBookData sbd = SpellBookData.getSpellBookData(itemStack);
            if (sbd.getSpellSlots() > 0) {
                var spell = sbd.getSpell(slot);
                if (spell != null) {
                    var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
                    if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() != spell.getLegacyID()) {
                        ServerboundCancelCast.cancelCast(serverPlayer, playerMagicData.getCastType() != CastType.LONG);
                    }
                    spell.attemptInitiateCast(itemStack, serverPlayer.level, serverPlayer, CastSource.SPELLBOOK, true);
                }
            }
        });
        return true;
    }
}
