package io.redspace.ironsspellbooks.effect;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.LivingEntity;

public enum OakskinData {
    INSTANCE;
    public static final Codec<OakskinData> CODEC = Codec.unit(INSTANCE);


    public static OakskinData setFromElixir(LivingEntity entity) {
        ((MagicData.IExtendedEntity) entity).irons_spellbooks$setOakskinData();
        return OakskinData.INSTANCE;
    }

    public static void remove(LivingEntity livingEntity) {
        ((MagicData.IExtendedEntity) livingEntity).irons_spellbooks$removeOakskinData();
    }

    public static boolean hasFromElixir(LivingEntity livingEntity) {
        return ((MagicData.IExtendedEntity) livingEntity).irons_spellbooks$hasOakskinData();
    }
}
