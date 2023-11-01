package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;

import java.util.Optional;

public class AnimationHolder {
    private final AnimationBuilder geckoAnimation;
    private final ResourceLocation playerAnimation;
    public final boolean adjustLeftArm;
    public final boolean adjustRightArm;

    public AnimationHolder(String path, ILoopType loopType) {
        this.playerAnimation = IronsSpellbooks.id(path);
        this.geckoAnimation = new AnimationBuilder().addAnimation(playerAnimation.getPath(), loopType);
        this.adjustLeftArm = true;
        this.adjustRightArm = true;
    }

    private AnimationHolder() {
        this.playerAnimation = null;
        this.geckoAnimation = null;
        this.adjustLeftArm = false;
        this.adjustRightArm = false;
    }
    private static final AnimationHolder empty = new AnimationHolder();
    private static final AnimationHolder pass = new AnimationHolder();

    /**
     * This singleton represents an animation where the player immediately stops animating at the end of a cast
     */
    public static AnimationHolder none() {
        return empty;
    }

    /**
     * This singleton lets the previous animation (the cast start animation) continue to play after the spell ends, so long as the spell wasn't cancelled
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
