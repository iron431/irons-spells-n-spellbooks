package io.redspace.skillcasting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.cast.ActiveCast;
import io.redspace.skillcasting.data.SkillcastingData;
import net.minecraft.client.renderer.MultiBufferSource;

public interface LevelRenderable {
    void render(PoseStack poseStack, MultiBufferSource buf, float partialTick, CasterRef casterRef, SkillcastingData data, ActiveCast activeCast);
}
