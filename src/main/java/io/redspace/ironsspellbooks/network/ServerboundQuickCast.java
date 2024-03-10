package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
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
            if (sbd.getSpellSlots() > 0 && slot < sbd.getSpellSlots()) {
                var spell = sbd.getSpell(slot);
                if (spell != null) {
                    var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
                    if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() != spell.getID()) {
                        ServerboundCancelCast.cancelCast(serverPlayer, playerMagicData.getCastType() != CastType.LONG);
                    }
                    spell.attemptInitiateCast(itemStack, serverPlayer.level, serverPlayer, CastSource.SPELLBOOK, true);
                }
            }
        });
        return true;
    }
}
