package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.Optional;

public class AnimationHolder {
    private final RawAnimation geckoAnimation;
    private final ResourceLocation playerAnimation;
    public final boolean adjustLeftArm;
    public final boolean adjustRightArm;

    public AnimationHolder(String path, Animation.LoopType loopType) {
        this.playerAnimation = IronsSpellbooks.id(path);
        this.geckoAnimation = RawAnimation.begin().then(playerAnimation.getPath(), loopType);
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

    public static AnimationHolder none() {
        return empty;
    }

    public Optional<RawAnimation> getForMob() {
        return geckoAnimation == null ? Optional.empty() : Optional.of(geckoAnimation);
    }

    public Optional<ResourceLocation> getForPlayer() {
        return playerAnimation == null ? Optional.empty() : Optional.of(playerAnimation);

    }
}
