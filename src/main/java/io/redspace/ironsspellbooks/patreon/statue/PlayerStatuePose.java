package io.redspace.ironsspellbooks.patreon.statue;

import com.mojang.serialization.Codec;

public record PlayerStatuePose(String name) {
    public static final Codec<PlayerStatuePose> CODEC = Codec.STRING.xmap(PlayerStatuePose::new, PlayerStatuePose::name);

    public static PlayerStatuePose NEUTRAL = new PlayerStatuePose("neutral");
    public static PlayerStatuePose ATTENTION = new PlayerStatuePose("attention");
    public static PlayerStatuePose SQUAT = new PlayerStatuePose("squat");
    public static PlayerStatuePose DEFAULT = NEUTRAL;
}
