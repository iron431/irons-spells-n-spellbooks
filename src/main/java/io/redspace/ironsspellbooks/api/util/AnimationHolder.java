package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;

import java.util.Optional;

public class AnimationHolder {
    private final AnimationBuilder geckoAnimation;
    private final ResourceLocation playerAnimation;
    public final boolean isPass;

    public AnimationHolder(String path, boolean playOnce) {
        this.playerAnimation = IronsSpellbooks.id(path);
        this.geckoAnimation = new AnimationBuilder().addAnimation(playerAnimation.getPath(), playOnce ? ILoopType.EDefaultLoopTypes.PLAY_ONCE : ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
        this.isPass = false;
    }

    private AnimationHolder(boolean isPass) {
        this.playerAnimation = null;
        this.geckoAnimation = null;
        this.isPass = isPass;
    }
    private static final AnimationHolder empty = new AnimationHolder(false);
    private static final AnimationHolder pass = new AnimationHolder(true);

    /**
     * Represents an empty animation, making the player immediately stop animating at the end of a cast
     */
    public static AnimationHolder none() {
        return empty;
    }

    /**
     * Represents the lack of an animation, letting the previous animation (the cast start animation) continue to play after the spell ends, so long as the spell wasn't cancelled
     */
    public static AnimationHolder pass() {
        return pass;
    }

    public Optional<AnimationBuilder> getForMob() {
        return geckoAnimation == null ? Optional.empty() : Optional.of(geckoAnimation);
    }

    public Optional<ResourceLocation> getForPlayer() {
        return playerAnimation == null ? Optional.empty() : Optional.of(playerAnimation);

    }
}
