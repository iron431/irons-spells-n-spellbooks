package io.redspace.skillcasting.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public class IronsAdjustmentModifier extends AbstractModifier {
    public boolean enabled = true;

    protected BiFunction<String, Float, Optional<AdjustmentModifier.PartModifier>> transformFunction;

    public IronsAdjustmentModifier(BiFunction<String, Float, Optional<AdjustmentModifier.PartModifier>> transformFunction) {
        this.transformFunction = transformFunction;
    }

    private static final int FADE_TICKS = 3;
    private @Nullable KeyframeAnimationPlayer animation;
    private int fadeOutTicks = 1;
    private int ticksSinceFadeOut;

    public void follow(KeyframeAnimationPlayer animation) {
        this.animation = animation;
    }

    public void fadeOut(int ticks) {
        this.animation = null;
        this.fadeOutTicks = ticks;
        this.ticksSinceFadeOut = 0;
    }

    @Override
    public void tick() {
        super.tick();
        ticksSinceFadeOut++;
    }

    // follows the animation's own tick so looping and speed-scaled animations keep the adjustment
    private float getFade(float partialTick) {
        if (animation == null) {
            return 1 - Mth.clamp((ticksSinceFadeOut + partialTick) / fadeOutTicks, 0, 1);
        }
        if (animation.isInfinite()) {
            return 1;
        }
        int end = Math.max(5, animation.getData().endTick); // prevent ultra-short animations from fading while its actually playing
        return Mth.clamp((end - (animation.getTick() + partialTick)) / FADE_TICKS, 0, 1);
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float partialTick, Vec3f value0) {
        if (!enabled) {
            return super.get3DTransform(modelName, type, partialTick, value0);
        }

        Optional<AdjustmentModifier.PartModifier> partModifier = transformFunction.apply(modelName, partialTick);

        Vec3f modifiedVector = value0;
        float fade = getFade(partialTick);
        if (partModifier.isPresent()) {
            modifiedVector = super.get3DTransform(modelName, type, partialTick, modifiedVector);
            return transformVector(modifiedVector, type, partModifier.get(), fade);
        } else {
            return super.get3DTransform(modelName, type, partialTick, value0);
        }
    }

    protected Vec3f transformVector(Vec3f vector, TransformType type, AdjustmentModifier.PartModifier partModifier, float fade) {
        switch (type) {
            case POSITION:
                return vector.add(partModifier.offset().scale(fade));
            case ROTATION:
                return vector.add(partModifier.rotation().scale(fade));
            case SCALE:
                return vector.add(partModifier.scale().scale(fade));
            case BEND:
                break;
        }
        return vector;
    }
}