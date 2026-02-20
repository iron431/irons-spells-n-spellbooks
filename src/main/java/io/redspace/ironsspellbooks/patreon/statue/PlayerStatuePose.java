package io.redspace.ironsspellbooks.patreon.statue;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PlayerStatuePose implements StringRepresentable {
    NEUTRAL,
    ATTENTION,
    SQUAT,
    REACHING,
    POINTING_FORWARD,
    FALLEN,
    STATUE,
    ;
    public static final Codec<PlayerStatuePose> CODEC = StringRepresentable.fromEnum(PlayerStatuePose::values);
    private static final Map<String, PlayerStatuePose> BY_NAME = Arrays.stream(PlayerStatuePose.values()).collect(Collectors.toMap(PlayerStatuePose::getSerializedName, Function.identity()));

    public static PlayerStatuePose defaultPose() {
        return NEUTRAL;
    }

    public static @NotNull PlayerStatuePose fromString(String pose) {
        return BY_NAME.getOrDefault(pose, defaultPose());
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
