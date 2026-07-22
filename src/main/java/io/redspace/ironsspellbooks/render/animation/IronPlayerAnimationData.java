package io.redspace.ironsspellbooks.render.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class IronPlayerAnimationData<T extends IAnimation> extends ModifierLayer<T> {
    IronsAdjustmentModifier adjustmentModifier;
    SpeedModifier speedModifier;
    MirrorModifier mirrorModifier;

    public IronPlayerAnimationData(AbstractClientPlayer player) {
        var animation = this;
        this.mirrorModifier = new MirrorModifier() {
            @Override
            public boolean isEnabled() {
                return MagicData.get(player).getCachedCastingEquipmentSlot().equals(EquipmentSlot.OFFHAND.getName()) ^ player.getMainArm() == HumanoidArm.LEFT;
            }
        };
        this.speedModifier = new SpeedModifier(1f);
        this.adjustmentModifier = new IronsAdjustmentModifier((partName, partialTick) -> {
            boolean handleHead = animation.getAnimation() != null && !animation.getAnimation().get3DTransform("head", TransformType.ROTATION, 0.5f, Vec3f.ZERO).equals(Vec3f.ZERO);
            switch (partName) {
                case "head" -> {
                    if (handleHead) {
                        return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(0, Mth.rotLerp(partialTick, (player.yHeadRotO - player.yBodyRotO), (player.yHeadRot - player.yBodyRot)) * Mth.DEG_TO_RAD, 0), Vec3f.ZERO));
                    } else {
                        return Optional.empty();
                    }
                }
                case "rightArm", "leftArm" -> {
                    float x = Mth.wrapDegrees(Mth.rotLerp(partialTick, player.xRotO, player.getXRot()) * 0.65f);
                    float y = Mth.wrapDegrees(Mth.rotLerp(partialTick, Mth.wrapDegrees(player.yHeadRotO - player.yBodyRotO), Mth.wrapDegrees(player.yHeadRot - player.yBodyRot)) * 0.65f);
                    Vec3f posAdjustment = Vec3f.ZERO;
                    if (animation.getAnimation() != null) {
                        Vec3f currentPos = animation.getAnimation().get3DTransform(partName, TransformType.POSITION, partialTick, Vec3f.ZERO);
                        Vec3 rotatedPos = new Vec3(currentPos.getX(), currentPos.getY(), currentPos.getZ()).xRot(-x * Mth.DEG_TO_RAD).yRot(y * Mth.DEG_TO_RAD);
                        posAdjustment = new Vec3f((float) (rotatedPos.x - currentPos.getX()), (float) (rotatedPos.y - currentPos.getY()), (float) (rotatedPos.z - currentPos.getZ()));
                    }
                    return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(x * Mth.DEG_TO_RAD, y * Mth.DEG_TO_RAD, 0), posAdjustment));
                }
                default -> {
                    return Optional.empty();
                }
            }
        });
        this.addModifier(mirrorModifier, 0); // ensure mirror modifier applies after adjustment modifier
        this.addModifier(speedModifier, 0);
        this.addModifier(adjustmentModifier, 0);
    }
}
