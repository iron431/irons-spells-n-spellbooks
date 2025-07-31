package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.Optional;

public class AnimationHolder {
    private final RawAnimation geckoAnimation;
    private final ResourceLocation playerAnimation;
    public final boolean isPass;
    public final boolean animatesLegs;

    /**
     * Use resource-location sensitive {@link AnimationHolder#AnimationHolder(ResourceLocation, boolean, boolean)}
     */
    @Deprecated(forRemoval = true)
    public AnimationHolder(String path, boolean playOnce, boolean animatesLegs) {
        this(path.contains(":") ? ResourceLocation.parse(path) : IronsSpellbooks.id(path), playOnce, animatesLegs);
    }

    public AnimationHolder(ResourceLocation animation, boolean playOnce, boolean animatesLegs) {
        this.playerAnimation = animation;
        this.geckoAnimation = RawAnimation.begin().then(playerAnimation.getPath(), playOnce ? Animation.LoopType.PLAY_ONCE : Animation.LoopType.HOLD_ON_LAST_FRAME);
        this.isPass = false;
        this.animatesLegs = animatesLegs;
    }

    public AnimationHolder(String path, boolean playOnce) {
        this(path, playOnce, false);
    }

    private AnimationHolder(boolean isPass) {
        this.playerAnimation = null;
        this.geckoAnimation = null;
        this.isPass = isPass;
        this.animatesLegs = false;
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

    public Optional<RawAnimation> getForMob() {
        return geckoAnimation == null ? Optional.empty() : Optional.of(geckoAnimation);
    }

    public Optional<ResourceLocation> getForPlayer() {
        return playerAnimation == null ? Optional.empty() : Optional.of(playerAnimation);

    }
}
