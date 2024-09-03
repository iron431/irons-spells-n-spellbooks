package io.redspace.ironsspellbooks.network.gui;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SelectSpellPacket implements CustomPacketPayload {
    private final SpellSelection spellSelection;
    public static final CustomPacketPayload.Type<SelectSpellPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "select_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectSpellPacket> STREAM_CODEC = CustomPacketPayload.codec(SelectSpellPacket::write, SelectSpellPacket::new);

    public SelectSpellPacket(SpellSelection spellSelection) {
        this.spellSelection = spellSelection;
    }

    public SelectSpellPacket(FriendlyByteBuf buf) {
        var tmpSpellSelection = new SpellSelection();
        tmpSpellSelection.readFromBuffer(buf);
        this.spellSelection = tmpSpellSelection;
    }

    public void write(FriendlyByteBuf buf) {
        spellSelection.writeToBuffer(buf);
    }

    public static void handle(SelectSpellPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (Log.SPELL_SELECTION) {
                    IronsSpellbooks.LOGGER.debug("ServerboundSelectSpell.handle {}", packet.spellSelection);
                }
                MagicData.getMagicData(serverPlayer).getSyncedData().setSpellSelection(packet.spellSelection);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
