package io.redspace.ironsspellbooks.gui.overlays;

import io.redspace.ironsspellbooks.api.network.ISerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;

public class SpellWheelSelection implements ISerializable, INBTSerializable<CompoundTag> {
    public String equipmentSlot = null;
    public int index = -1;
    public String lastEquipmentSlot = null;
    public int lastIndex = -1;

    public SpellWheelSelection() {
    }

    public SpellWheelSelection(String equipmentSlot, int index, String lastEquipmentSlot, int lastIndex) {
        this.equipmentSlot = equipmentSlot;
        this.index = index;
        this.lastEquipmentSlot = lastEquipmentSlot;
        this.lastIndex = lastIndex;
    }

    public boolean isEmpty() {
        return index < 0;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(equipmentSlot);
        buffer.writeInt(index);
        buffer.writeUtf(lastEquipmentSlot);
        buffer.writeInt(lastIndex);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        equipmentSlot = buffer.readUtf();
        index = buffer.readInt();
        lastEquipmentSlot = buffer.readUtf();
        lastIndex = buffer.readInt();
    }

    @Override
    public CompoundTag serializeNBT() {
        var compoundTag = new CompoundTag();
        compoundTag.putString("slot", equipmentSlot);
        compoundTag.putInt("index", index);
        compoundTag.putString("lastSlot", lastEquipmentSlot);
        compoundTag.putInt("lastIndex", lastIndex);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        equipmentSlot = compoundTag.getString("slot");
        index = compoundTag.getInt("index");
        lastEquipmentSlot = compoundTag.getString("lastSlot");
        lastIndex = compoundTag.getInt("lastIndex");
    }
}