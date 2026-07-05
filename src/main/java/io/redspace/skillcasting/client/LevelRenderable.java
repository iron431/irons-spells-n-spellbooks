package io.redspace.skillcasting.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.client.renderer.MultiBufferSource;

public interface LevelRenderable {
    void render(PoseStack poseStack, MultiBufferSource buf, float partialTick, CasterRef casterRef, SkillcastingData data, ActiveCast activeCast);
}
