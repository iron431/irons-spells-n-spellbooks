package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcastingapi.core.SkillcastManager;
import io.redspace.skillcastingapi.data.SkillcastingData;
import io.redspace.skillcastingapi.data.caster_id.EntityCasterId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CancelCastPacket implements CustomPacketPayload {
    //    private final boolean triggerCooldown;
    public static final CustomPacketPayload.Type<CancelCastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "cancel_cast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelCastPacket> STREAM_CODEC = CustomPacketPayload.codec(CancelCastPacket::write, CancelCastPacket::new);

    public CancelCastPacket() {
//        this.triggerCooldown = triggerCooldown;
    }

    public CancelCastPacket(FriendlyByteBuf buf) {
//        triggerCooldown = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
//        buf.writeBoolean(triggerCooldown);
    }

    public static void cancelCast(ServerPlayer serverPlayer, boolean triggerCooldown) {
        if (serverPlayer != null) {
            var playerMagicData = SkillcastingData.get(serverPlayer);
            if (playerMagicData.isCasting()) {
                SkillcastManager.cancelCast(EntityCasterId.of(serverPlayer));
//                if (triggerCooldown) {
////                    MagicHelper.MAGIC_MANAGER.addCooldown(serverPlayer, spellData.getSpell(), playerMagicData.getCastSource());
//                    throw new NotImplementedException();
//                }
//                if (playerMagicData.getCastSource() == CastSource.SCROLL && spellData.getSpell().getCastType() == CastType.CONTINUOUS) {
//                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
//                }
//                playerMagicData.getCastingSpell().getSpell().onServerCastComplete(serverPlayer.level, spellData.getLevel(), serverPlayer, playerMagicData, true);
            }
        }
    }

    public static void handle(CancelCastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SkillcastManager.cancelCast(EntityCasterId.of(serverPlayer));
//                cancelCast(serverPlayer, packet.triggerCooldown);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
