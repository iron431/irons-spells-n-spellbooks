package io.redspace.skillcasting.cooldown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class CooldownInstance {
    public static final Codec<CooldownInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("total").forGetter(CooldownInstance::totalTicks),
            Codec.INT.fieldOf("remaining_ticks").forGetter(CooldownInstance::remainingTicks)
    ).apply(builder, CooldownInstance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CooldownInstance::totalTicks,
            ByteBufCodecs.VAR_INT, CooldownInstance::remainingTicks,
            CooldownInstance::new);

    private final int totalTicks;
    private int remainingTicks;

    public CooldownInstance(int totalTicks, int remainingTicks) {
        this.totalTicks = totalTicks;
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public static CooldownInstance of(int totalTicks) {
        return new CooldownInstance(totalTicks, totalTicks);
    }

    public int totalTicks() {
        return totalTicks;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public boolean isFinished() {
        return remainingTicks <= 0;
    }

    public float getCooldownPercent() {
        if (totalTicks <= 0) {
            return 0;
        }
        return remainingTicks / (float) totalTicks;
    }
}
