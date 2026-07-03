package io.redspace.ironsspellbooks.effect;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;

public enum OakskinData {
    INSTANCE;
    public static final Codec<OakskinData> CODEC = Codec.unit(INSTANCE);


    public static OakskinData setFromElixir(LivingEntity entity) {
        return entity.setData(DataAttachmentRegistry.OAKSKIN_FROM_ELIXIR, INSTANCE);
    }

    public static void remove(LivingEntity livingEntity) {
        livingEntity.removeData(DataAttachmentRegistry.OAKSKIN_FROM_ELIXIR);
    }

    public static boolean hasFromElixir(LivingEntity livingEntity) {
        return livingEntity.hasData(DataAttachmentRegistry.OAKSKIN_FROM_ELIXIR);
    }
}
