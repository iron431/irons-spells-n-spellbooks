package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;

import java.util.Optional;

public class AnimationHolder {
    private final AnimationBuilder geckoAnimation;
    private final ResourceLocation playerAnimation;

    public AnimationHolder(String path, ILoopType loopType) {
        this.playerAnimation = IronsSpellbooks.id(path);
        this.geckoAnimation = new AnimationBuilder().addAnimation(playerAnimation.getPath(), loopType);
    }

    private AnimationHolder() {
        this.playerAnimation = null;
        this.geckoAnimation = null;
    }
    private static final AnimationHolder empty = new AnimationHolder();
    public static AnimationHolder none() {
        return empty;
    }

    public Optional<AnimationBuilder> getForMob() {
        return geckoAnimation == null ? Optional.empty() : Optional.of(geckoAnimation);
    }

    public Optional<ResourceLocation> getForPlayer() {
        return playerAnimation == null ? Optional.empty() : Optional.of(playerAnimation);

    }
}
