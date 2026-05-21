package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncMagicDataPacket implements CustomPacketPayload {
    MagicData data;
    int entityId;
    public static final Type<SyncMagicDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_magic_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMagicDataPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncMagicDataPacket::write, SyncMagicDataPacket::new);

    public SyncMagicDataPacket(MagicData data, Entity entity) {
        this.data = data;
        this.entityId = entity.getId();
    }

    public SyncMagicDataPacket(FriendlyByteBuf buffer) {
        entityId = buffer.readInt();
        String castingSpellId = buffer.readUtf();
        int castingSpellLevel = buffer.readInt();
        long start = buffer.readVarLong();
        long finish = buffer.readVarLong();
        float heartstopDamage = buffer.readFloat();
        SpinAttackType spinAttackType = new SpinAttackType(buffer.readResourceLocation(), buffer.readBoolean());
        String castingEquipmentSlot = buffer.readUtf();
        this.data = new MagicData(null);
        data.getLearnedSpelLData().readFromBuffer(buffer);
        data.getSpellSelection().readFromBuffer(buffer);
        data.recreateSpell(castingSpellId, castingSpellLevel, start, finish, castingEquipmentSlot);
        data.setHeartstopAccumulatedDamage(heartstopDamage);
        data.setSpinAttackType(spinAttackType);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeUtf(data.getCastingSpellId());
        buffer.writeInt(data.getCastingSpellLevel());
        buffer.writeVarLong(data.getCastStartTimestamp());
        buffer.writeVarLong(data.getCastEndTimestamp());
        buffer.writeFloat(data.getHeartstopAccumulatedDamage());
//        buffer.writeInt(data.evasionHitsRemaining);
        buffer.writeResourceLocation(data.getSpinAttackType().textureId());
        buffer.writeBoolean(data.getSpinAttackType().fullbright());
        buffer.writeUtf(data.getCastingEquipmentSlot());
        data.getLearnedSpelLData().writeToBuffer(buffer);
        data.getSpellSelection().writeToBuffer(buffer);
    }

    public static void handle(SyncMagicDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            MinecraftInstanceHelper.ifPlayerPresent(player -> {
                if (player.level.getEntity(packet.entityId) instanceof LivingEntity livingEntity) {
                    MagicData existingData = livingEntity.getData(DataAttachmentRegistry.MAGIC_DATA);
                    existingData.applyClientSyncData(packet.data);
                }
            });
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
