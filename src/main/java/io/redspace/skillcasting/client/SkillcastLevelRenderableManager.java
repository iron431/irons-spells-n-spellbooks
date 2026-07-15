package io.redspace.skillcasting.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class SkillcastLevelRenderableManager {
    private static final List<Wrapped> RENDERABLES = new ArrayList<>();

    private record Wrapped(CasterRef casterRef, LevelRenderable renderable, RenderInfoMutable renderInfo) {
    }

    private static class RenderInfoMutable {
        Vec3 oldPos = Vec3.ZERO;
        Vec3 keyPos = Vec3.ZERO;
        Vec3 oldDir = Vec3.ZERO;
        Vec3 keyDir = Vec3.ZERO;

        void handleUpdate(Vec3 pos, Vec3 dir) {
            // catch rising edge and automatically update ticking
            if (!keyPos.equals(pos) || oldPos == Vec3.ZERO) {
                oldPos = keyPos;
                keyPos = pos;
            }
            if (!keyDir.equals(dir) || oldDir == Vec3.ZERO) {
                oldDir = keyDir;
                keyDir = dir;
            }
        }
    }

    public static void clear() {
        RENDERABLES.clear();
    }

    @SubscribeEvent
    public static void levelRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        if (RENDERABLES.isEmpty()) {
            return;
        }
        MultiBufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();
        List<Wrapped> toRemove = new ArrayList<>();
        for (Wrapped wrapped : RENDERABLES) {
            CasterRef casterRef = wrapped.casterRef;
            LevelRenderable renderable = wrapped.renderable;
            if (!casterRef.isValid()) {
                toRemove.add(wrapped);
                continue;
            }
            SkillcastingData data = casterRef.skillcastingData();
            ActiveCast activeCast = data.getActiveCast();
            if (activeCast == null) {
                toRemove.add(wrapped);
                continue;
            }
            PoseStack poseStack = new PoseStack();
            Vec3 castingPosition = activeCast.context().position(PositionAnchor.CASTING_POSITION_CENTER);
            Vec3 castingDirection = activeCast.context().direction();
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            wrapped.renderInfo.handleUpdate(castingPosition, castingDirection);
            Vec3 renderPos = wrapped.renderInfo.oldPos.lerp(castingPosition, partialTick).subtract(event.getCamera().getPosition());
            Vec3 renderDir = wrapped.renderInfo.oldDir.lerp(castingDirection, partialTick);
            float pitch = (float) Math.asin(renderDir.y);
            float yaw = (float) Math.atan2(renderDir.x, renderDir.z);

            poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw * Mth.RAD_TO_DEG));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch * Mth.RAD_TO_DEG));
            try {
                renderable.render(poseStack, buf, partialTick, casterRef, data, activeCast);
            } catch (Exception e) {
                Skillcasting.LOGGER.error("Failed to render renderable for skillcast {}: {}", activeCast.context(), e.getMessage());
                toRemove.add(wrapped);
            }
        }
        RENDERABLES.removeAll(toRemove);
    }

    public static void track(CasterRef casterRef, LevelRenderable ticker) {
        RENDERABLES.add(new Wrapped(casterRef, ticker, new RenderInfoMutable()));
    }
}
