package io.redspace.ironsspellbooks.render.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.util.Mth;

import java.util.Optional;
import java.util.function.BiFunction;

public class IronsAdjustmentModifier extends AbstractModifier {
    public boolean enabled = true;

    protected BiFunction<String, Float, Optional<AdjustmentModifier.PartModifier>> transformFunction;

    public IronsAdjustmentModifier(BiFunction<String, Float, Optional<AdjustmentModifier.PartModifier>> transformFunction) {
        this.transformFunction = transformFunction;
    }

    int duration;
    int currentTick;
    int fadeTime;

    public void setupFade(int duration, int fadeTime) {
        this.currentTick = 0;
        this.duration = Math.max(5, duration); // prevent ultra-short animations from fading while its actually playing
        this.fadeTime = fadeTime;
    }

    @Override
    public void tick() {
        super.tick();
        currentTick++;
    }

    public float getFadeOutPercent(float partialTick) {
        if (currentTick > duration) {
            return 1;
        }
        return Mth.clamp(((currentTick + partialTick) - (duration - fadeTime)) / fadeTime, 0, 1);
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float partialTick, Vec3f value0) {
        if (!enabled) {
            return super.get3DTransform(modelName, type, partialTick, value0);
        }

        Optional<AdjustmentModifier.PartModifier> partModifier = transformFunction.apply(modelName, partialTick);

        Vec3f modifiedVector = value0;
        float fade = 1 - getFadeOutPercent(partialTick);
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