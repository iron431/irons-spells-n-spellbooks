package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
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

            var spellbookStack = Utils.getPlayerSpellbookStack(serverPlayer);
            SpellBookData sbd = SpellBookData.getSpellBookData(spellbookStack);
            if (sbd.getSpellSlots() > 0) {
                var spellData = sbd.getSpell(slot);
                if (spellData != null) {
                    var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
                    if (playerMagicData.isCasting() && !playerMagicData.getCastingSpellId().equals(spellData.getSpell().getSpellId())) {
                        ServerboundCancelCast.cancelCast(serverPlayer, playerMagicData.getCastType() != CastType.LONG);
                    }
                    spellData.getSpell().attemptInitiateCast(spellbookStack, spellData.getLevel(), serverPlayer.level, serverPlayer, CastSource.SPELLBOOK, true);
                }
            }
        });
        return true;
    }
}
