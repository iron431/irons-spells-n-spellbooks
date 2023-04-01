package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundQuickCast {

    private int slot;

    public ServerboundQuickCast(int slot) {
        this.slot = slot;
    }

    public ServerboundQuickCast(FriendlyByteBuf buf) {
        slot = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            var itemStack = serverPlayer.getMainHandItem();
            SpellBookData sbd = SpellBookData.getSpellBookData(itemStack);
            if (sbd.getSpellSlots() > 0) {
                var spell = sbd.getSpell(slot);
                if (spell != null) {
                    var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
                    if (playerMagicData.isCasting()) {
                        ServerboundCancelCast.cancelCast(serverPlayer, playerMagicData.getCastType() != CastType.LONG);
                    }
                    spell.attemptInitiateCast(itemStack, serverPlayer.level, serverPlayer, CastSource.SPELLBOOK, true);
                }
            }
        });
        return true;
    }
}
