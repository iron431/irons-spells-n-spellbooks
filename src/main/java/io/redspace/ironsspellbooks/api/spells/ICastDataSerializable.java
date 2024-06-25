package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.network.ISerializable;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;


public interface ICastDataSerializable extends ICastData, ISerializable, INBTSerializable<CompoundTag> {
}
