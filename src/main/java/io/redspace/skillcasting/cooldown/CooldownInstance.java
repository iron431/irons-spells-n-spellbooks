package io.redspace.skillcasting.cooldown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.SkillcastingTime;

public record CooldownInstance(int totalTicks, long endsAtGameTime) {
    public static final Codec<CooldownInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("total").forGetter(CooldownInstance::totalTicks),
            Codec.LONG.fieldOf("ends_at").forGetter(CooldownInstance::endsAtGameTime)
    ).apply(builder, CooldownInstance::new));

    public static CooldownInstance startingNow(int totalTicks, long gameTime) {
        return new CooldownInstance(totalTicks, SkillcastingTime.endsAt(gameTime, totalTicks));
    }

    public int remainingTicks(long gameTime) {
        return SkillcastingTime.remainingTicks(gameTime, endsAtGameTime);
    }

    public boolean isFinished(long gameTime) {
        return SkillcastingTime.isExpired(gameTime, endsAtGameTime);
    }

    public float getCooldownPercent(long gameTime) {
        if (totalTicks <= 0) {
            return 0;
        }
        return remainingTicks(gameTime) / (float) totalTicks;
    }
}
