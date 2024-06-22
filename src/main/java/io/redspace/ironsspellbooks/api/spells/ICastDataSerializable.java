package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.network.ISerializable;
import net.minecraft.nbt.CompoundTag;


public interface ICastDataSerializable extends ICastData, ISerializable, INBTSerializable<CompoundTag> {
}
