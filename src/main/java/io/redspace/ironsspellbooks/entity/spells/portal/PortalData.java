package io.redspace.ironsspellbooks.entity.spells.portal;

import io.redspace.skillcastingapi.data.ICastDataSerializable;

public class PortalData implements ICastDataSerializable<PortalData> {
//    public PortalPos globalPos1;
//    public UUID portalEntityId1;
//    public PortalPos globalPos2;
//    public UUID portalEntityId2;
//    public int ticksToLive;
//    public boolean isBlock;
//
//    public PortalData() {
//    }
//
//    public void setPortalDuration(int ticksToLive) {
//        this.ticksToLive = ticksToLive;
//    }
//
//    public void firstPortal(UUID uuid, PortalPos pos) {
//        this.portalEntityId1 = uuid;
//        this.globalPos1 = pos;
//    }
//
//    public void secondPortal(UUID uuid, PortalPos pos) {
//        this.portalEntityId2 = uuid;
//        this.globalPos2 = pos;
//    }
//
//    public Optional<PortalPos> getConnectedPortalPos(UUID portalId) {
//        if (portalEntityId1.equals(portalId)) {
//            return Optional.of(globalPos2);
//        } else if (portalEntityId2.equals(portalId)) {
//            return Optional.of(globalPos1);
//        }
//
//        return Optional.empty();
//    }
//
//    public UUID getConnectedPortalUUID(UUID portalId) {
//        if (portalEntityId1.equals(portalId)) {
//            return portalEntityId2;
//        } else if (portalEntityId2.equals(portalId)) {
//            return portalEntityId1;
//        }
//
//        return null;
//    }
//
//    //TODO: make buffer utils class?
//    private void writePortalPosToBuffer(FriendlyByteBuf buffer, PortalPos pos) {
//        buffer.writeResourceKey(pos.dimension());
//        Vec3 vec3 = pos.pos();
//        buffer.writeInt((int) (vec3.x * 10));
//        buffer.writeInt((int) (vec3.y * 10));
//        buffer.writeInt((int) (vec3.z * 10));
//        buffer.writeFloat(pos.rotation());
//    }
//
//    private PortalPos readPortalPosFromBuffer(FriendlyByteBuf buffer) {
//        return PortalPos.of(buffer.readResourceKey(Registries.DIMENSION), new Vec3(buffer.readInt() / 10.0, buffer.readInt() / 10.0, buffer.readInt() / 10.0), buffer.readFloat());
//    }
//
//
//    @Override
//    public void reset() {
//        //nothing to clean up for Portal
//    }
//
//    public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
//            Codec.INT.fieldOf("ticksToLive").forGetter(data -> data.ticksToLive),
//            Codec.BOOL.fieldOf("isBlock").forGetter(data -> data.isBlock),
//
//            ))
//
//    @Override
//    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
//        CompoundTag tag = new CompoundTag();
//        tag.putInt("ticksToLive", ticksToLive);
//
//        if (globalPos1 != null) {
//            tag.put("gp1", NBT.writePortalPos(globalPos1));
//            tag.putUUID("pe1", portalEntityId1);
//
//            if (globalPos2 != null) {
//                tag.put("gp2", NBT.writePortalPos(globalPos2));
//                tag.putUUID("pe2", portalEntityId2);
//            }
//        }
//
//        tag.putBoolean("isBlock", isBlock);
//
//        return tag;
//    }
//
//    @Override
//    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
//        ticksToLive = compoundTag.getInt("ticksToLive");
//
//        if (compoundTag.contains("gp1") && compoundTag.contains("pe1")) {
//            this.globalPos1 = NBT.readPortalPos(compoundTag.getCompound("gp1"));
//            this.portalEntityId1 = compoundTag.getUUID("pe1");
//
//            if (compoundTag.contains("gp2") && compoundTag.contains("pe2")) {
//                this.globalPos2 = NBT.readPortalPos(compoundTag.getCompound("gp2"));
//                this.portalEntityId2 = compoundTag.getUUID("pe2");
//            }
//        }
//        this.isBlock = compoundTag.getBoolean("isBlock");
//    }
//
//    @Override
//    public String toString() {
//        return String.format("PortalData[pos1:%s pos2:%s id1:%s id2:%s]", this.globalPos1, this.globalPos2, this.portalEntityId1, this.portalEntityId2);
//    }
//
//    static final StreamCodec<RegistryFriendlyByteBuf, PortalData> STREAM_CODEC = StreamCodec.of(PortalData::writeToBuffer, PortalData::readFromBuffer);
//
//    @Override
//    public StreamCodec<RegistryFriendlyByteBuf, PortalData> streamCodec() {
//        return STREAM_CODEC;
//    }
//
//    @Override
//    public Codec<PortalData> codec() {
//        return null;
//    }
//
//    public static PortalData readFromBuffer(RegistryFriendlyByteBuf buffer) {
//        PortalData data = new PortalData();
//        data.ticksToLive = buffer.readInt();
//        if (buffer.readBoolean()) {
//            data.globalPos1 = data.readPortalPosFromBuffer(buffer);
//            data.portalEntityId1 = buffer.readUUID();
//
//            if (buffer.readBoolean()) {
//                data.globalPos2 = data.readPortalPosFromBuffer(buffer);
//                data.portalEntityId2 = buffer.readUUID();
//            }
//        }
//        data.isBlock = buffer.readBoolean();
//        return data;
//    }
//
//    public static void writeToBuffer(RegistryFriendlyByteBuf buffer, PortalData data) {
//        buffer.writeInt(data.ticksToLive);
//
//        if (data.globalPos1 != null && data.portalEntityId1 != null) {
//            buffer.writeBoolean(true);
//            data.writePortalPosToBuffer(buffer, data.globalPos1);
//            buffer.writeUUID(data.portalEntityId1);
//
//            if (data.globalPos2 != null && data.portalEntityId2 != null) {
//                buffer.writeBoolean(true);
//                data.writePortalPosToBuffer(buffer, data.globalPos2);
//                buffer.writeUUID(data.portalEntityId2);
//            } else {
//                buffer.writeBoolean(false);
//            }
//        } else {
//            buffer.writeBoolean(false);
//        }
//        buffer.writeBoolean(data.isBlock);
//    }
}
